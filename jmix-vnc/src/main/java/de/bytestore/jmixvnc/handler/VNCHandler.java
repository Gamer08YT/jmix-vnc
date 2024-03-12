package de.bytestore.jmixvnc.handler;

import de.bytestore.jmixvnc.entity.VNCSession;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;

import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class VNCHandler {
    // Store Sockets of Proxied sessions.
    private static HashMap<String, Socket> socketIO = new HashMap<String, Socket>();

    // Store DataManager for Static Usage.
    private static DataManager dataIO;

    /**
     * Retrieves the Socket associated with the given ticketIO.
     *
     * @param ticketIO the ticketIO identifier of the Socket to retrieve
     * @return the Socket associated with the given ticketIO, or null if not found
     */
    public static Socket getSocket(String ticketIO) {
        if(socketIO.containsKey(ticketIO))
            return socketIO.get(ticketIO);

        return null;
    }

    /**
     * Retrieves the VNCSession object associated with the given ticketIO.
     *
     * @param ticketIO the ticketIO identifier of the VNCSession to retrieve
     * @return the VNCSession associated with the given ticketIO, or null if not found
     */
    public static VNCSession getSession(String ticketIO) {
        return dataIO.load(VNCSession.class).condition(PropertyCondition.equal("id", UUID.fromString(ticketIO))).one();
    }

    /**
     * Sets the Socket for the given ticketIO in the socketIO HashMap.
     *
     * @param ticketIO    the ticketIO identifier of the Socket
     * @param proxiedIO   the Socket to be associated with the ticketIO
     */
    public static void setSocket(String ticketIO, Socket proxiedIO) {
        socketIO.put(ticketIO, proxiedIO);
    }

    /**
     * Removes the Socket associated with the given ticketIO from the socketIO HashMap.
     *
     * @param ticketIO the ticketIO identifier of the Socket to remove
     */
    public static void removeSocket(String ticketIO) {
        socketIO.remove(ticketIO);
    }

    /**
     * Sets the specified DataManager as the dataIO in the VNCHandler class.
     *
     * @param dataManager the DataManager to set
     */
    public static void setDataManager(DataManager dataManager) {
        dataIO = dataManager;
    }
}
