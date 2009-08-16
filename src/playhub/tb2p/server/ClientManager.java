/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;


import naga.*;
import naga.packetreader.*;

import playhub.tb2p.protocol.*;
import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class ClientManager extends SocketObserverAdapter {

    protected static Logger logger = Logger.getLogger(ClientManager.class.getCanonicalName());
    private GameKeeper gk;
    
    public ClientManager(ServerSettings settings) {
        gk = new GameKeeper(settings);
    }

    @Override
    public void connectionOpened(NIOSocket socket) {
        // otherwise, we will accept the connection
        logger.fine("socket opened: "+socket.getIp()+":"+socket.getPort());
        socket.setPacketReader(new AsciiLinePacketReader());
        gk.registerSocket(socket);
    }

    @Override
    public void connectionBroken(NIOSocket socket, Exception e) {
        logger.fine("socket disconnected: "+socket.getIp()+":"+socket.getPort());
        gk.unregisterSocket(socket);
    }

    @Override
    public void packetReceived(NIOSocket socket, byte[] packet) {
        PDU pdu;
        try {
            pdu = PDU.parsedFromPacket(packet);
        }
        catch (MalformedPDUException mpe) {
            System.err.println(mpe.toString());
            socket.close();
            return;
        }
        gk.receivePDU(socket, pdu);
    }

}
