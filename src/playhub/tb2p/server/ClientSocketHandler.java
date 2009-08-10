/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;
import java.io.*;
import naga.*;
import naga.packetreader.*;

import playhub.tb2p.protocol.*;
import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class ClientSocketHandler extends SocketObserverAdapter {

    protected static Logger logger = Logger.getLogger("playhub.tb2p.server.Server");
    private ServerSettings settings;
    private int countConnections;

    public ClientSocketHandler(ServerSettings settings) {
        this.settings = settings;
        this.countConnections = 0;
    }

    @Override
    public void connectionOpened(NIOSocket socket) {
        if (this.countConnections >= settings.getMaxConnections()) {
            // deny this connection, we've reach the capacity of this server
            logger.info("socket denied: "+socket.getIp()+":"+socket.getPort());
            socket.close();
        }
        else {
            // otherwise, we will accept the connection
            logger.info("socket opened: "+socket.getIp()+":"+socket.getPort());
        }
        this.countConnections++;
        socket.setPacketReader(new AsciiLinePacketReader());
    }

    @Override
    public void connectionBroken(NIOSocket socket, Exception e) {
        logger.info("socket disconnected: "+socket.getIp()+":"+socket.getPort());
        this.countConnections--;
    }

    @Override
    public void packetReceived(NIOSocket socket, byte[] packet) {
        PDU pdu;
        try {
            pdu = PDU.parsedFromPacket(packet);
        }
        catch (MalformedPDUException mpe) {
            System.err.println(mpe.toString());
            return;
        }
        System.err.println("got pdu type="+pdu.getType().toString());
        socket.write(packet);
    }

}
