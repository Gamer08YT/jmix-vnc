package de.bytestore.jmixvnc.entity;

import de.bytestore.jmixvnc.handler.VNCHandler;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "NOVNV_VNC_SESSION")
@Entity(name = "novnv_VNCSession")
public class VNCSession {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "HOSTNAME", nullable = false)
    @NotNull
    private String hostname;

    @Column(name = "PORT")
    private Integer port;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Socket getSocket() {
        return VNCHandler.getSocket(this.id.toString());
    }

    public void close() {
        // Get Socket Connection.
        Socket socket = VNCHandler.getSocket(this.id.toString());

        // Check if Socket is not null.
        if(socket != null) {
                try {
                    // Close TCP Socket (VNC Server <-> JMIX Backend).
                    socket.close();

                    // Remove from VNC Handler.
                    VNCHandler.removeSocket(this.id.toString());

                    // @todo: remove from DB
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }