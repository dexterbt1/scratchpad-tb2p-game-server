/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;
import java.io.*;
import naga.*;

/**
 *
 * @author dexter
 */
public class ClientSocketHandler extends SocketObserverAdapter {

    protected static Logger logger = Logger.getLogger("playhub.tb2p.server.Server");
    private ServerSettings settings;
    private int count_connections;

    public ClientSocketHandler(ServerSettings settings) {
        this.settings = settings;
        this.count_connections = 0;
    }

    @Override
    public void connectionOpened(NIOSocket socket) {
        if (this.count_connections >= settings.getMaxConnections()) {
            // deny this connection, we've reach the capacity of this server
            logger.info("socket denied: "+socket.getIp()+":"+socket.getPort());
            socket.close();
        }
        else {
            // otherwise, we will accept the connection
            logger.info("socket opened: "+socket.getIp()+":"+socket.getPort());
        }
        this.count_connections++;
    }

    @Override
    public void connectionBroken(NIOSocket socket, Exception e) {
        logger.info("socket disconnected: "+socket.getIp()+":"+socket.getPort());
        this.count_connections--;
    }

    @Override
    public void packetReceived(NIOSocket socket, byte[] packet) {
        socket.write(packet);
    }

}
