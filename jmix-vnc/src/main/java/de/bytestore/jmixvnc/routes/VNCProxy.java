package de.bytestore.jmixvnc.routes;

import de.bytestore.jmixvnc.entity.VNCSession;
import de.bytestore.jmixvnc.handler.VNCHandler;
import io.jmix.core.JmixSecurityFilterChainOrder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

@Configuration
@EnableWebSocket
public class VNCProxy implements WebSocketConfigurer {
    protected static final Logger log = org.slf4j.LoggerFactory.getLogger(VNCProxy.class);

    /**
     * Creates a SecurityFilterChain for the /novnc/** path to allow all requests.
     *
     * @param http the HttpSecurity object to configure
     * @return the created SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    @Order(JmixSecurityFilterChainOrder.FLOWUI - 10)
    // https://forum.jmix.io/t/static-resources-problem/2351/10
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/novnc/**")
                .authorizeHttpRequests((authorize) -> authorize.requestMatchers("/novnc/**").permitAll());
        return http.build();
    }

    /**
     * Registers WebSocket handlers for VNC connections.
     *
     * @param registryIO the WebSocket handler registry to register the handlers with
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registryIO) {
        registryIO.addHandler(handleVNC(), "/novnc/{ticket}").setAllowedOrigins("*");
    }

    /**
     * Handles VNC WebSocket connections.
     *
     * @return a WebSocketHandler for VNC connections
     */
    @Bean
    protected WebSocketHandler handleVNC() {
        return new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession clientIO) throws Exception {
                String ticket = extractTicketFromUri(clientIO.getUri().toString());

                // Try to get Session from the registry.
                VNCSession sessionIO = VNCHandler.getSession(ticket);

                // Check if session exists.
                if(sessionIO != null) {
                    // Print Debug Message.
                    log.info("New VNC WebSocket Client connected with ticket: {}", ticket);

                    Socket proxiedIO;

                    try {
//                        if(sessionIO.getSocket().getCert() == null) {
                        // Create a new Socket by Backend VNC Server.
                        proxiedIO = new Socket(sessionIO.getHostname(), sessionIO.getPort());
//                        } else {
//                            log.info("WITH CERT");
//
//                            proxiedIO = NetworkHandler.createSSLSocketWithX509Encryption(NetworkHandler.parseCertificate(sessionIO.getSocket().getCert()), sessionIO.getSocket().getHostname(), sessionIO.getSocket().getPort());
//                        }


                        // Set Socket of Session.
                        VNCHandler.setSocket(ticket, proxiedIO);

                        // Set Session Object to WebSocket Client.
                        clientIO.getAttributes().put("novnc", sessionIO);

                        Thread readThread =  new Thread(new Runnable() {
                            public void run() {
                                try {
                                    byte[] b = new byte[1500];
                                    int readBytes;
                                    while (clientIO.isOpen()){
                                        readBytes = proxiedIO.getInputStream().read(b);

                                        if (readBytes == -1){
                                            break;
                                        }

                                        if (readBytes > 0) {
                                            clientIO.sendMessage(new BinaryMessage(ByteBuffer.wrap(b,0, readBytes)));
                                        }
                                    }
                                } catch (IOException e) {
                                    log.error("VNC Socket Error", e);
                                }
                            }
                        });
                        readThread.start();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Print Warn Message.
                    log.warn("Connection from '" + clientIO.getRemoteAddress().toString() + "' tried to use NoVNC Reverse Proxy with invalid Credentials '" + ticket + "'.");

                    // Close the Connection immediately.
                    clientIO.close(CloseStatus.SESSION_NOT_RELIABLE);
                }

            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> messageIO) throws Exception {
                if(messageIO instanceof BinaryMessage && session.getAttributes().containsKey("novnc")) {
                    BinaryMessage binaryIO = (BinaryMessage) messageIO;

                    // Init new Buffer with Payload length.
                    byte[] dataIO = new byte[binaryIO.getPayloadLength()];

                    // Write data into Buffer.
                    binaryIO.getPayload().get(dataIO);

                    // Write payload to VNC Server.
                    VNCHandler.getSocket(((VNCSession) session.getAttributes().get("novnc")).getId().toString()).getOutputStream().write(dataIO);
                } else {
                    // Print Information.
                    log.info("Closed Invalid VNC Session.");

                    // Close the Connection immediately.
                    session.close(CloseStatus.SESSION_NOT_RELIABLE);

                    // Remove from Database.
                    ((VNCSession) session.getAttributes().get("novnc")).close();
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                if(session.getAttributes().containsKey("novnc")) {
                    session.close();

                    // Close VNC Socket and remove from Database.
                    ((VNCSession) session.getAttributes().get("novnc")).close();
                }
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }

            private String extractTicketFromUri(String uri) {
                UriTemplate template = new UriTemplate("/novnc/{ticket}");
                return template.match(uri).get("ticket");
            }
        };
    }
}
