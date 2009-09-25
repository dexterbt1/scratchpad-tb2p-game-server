/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;


import naga.*;

import playhub.tb2p.protocol.*;
import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class ClientManager extends SocketObserverAdapter {

    protected static Logger logger = Logger.getLogger(ClientManager.class.getCanonicalName());
    private GameKeeper gk;
    private static ServerCipher servercipher;
        static {
            try {
                servercipher = new ServerCipher();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.exit(255);
            }
        }
    
    public ClientManager(ServerSettings settings) {
        try {
            gk = new GameKeeper(settings);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(255);
        }
    }

    @Override
    public void connectionOpened(NIOSocket socket) {
        // otherwise, we will accept the connection
        logger.fine("socket opened: "+socket.getIp()+":"+socket.getPort());
        socket.setPacketReader(new naga.packetreader.RegularPacketReader(2, true));
        socket.setPacketWriter(new naga.packetwriter.RegularPacketWriter(2, true));
        gk.registerSocket(socket);
    }

    @Override
    public void connectionBroken(NIOSocket socket, Exception e) {
        logger.fine("socket disconnected: "+socket.getIp()+":"+socket.getPort());
        gk.unregisterSocket(socket);
    }

    @Override
    public void packetReceived(NIOSocket socket, byte[] encrypted) {
        PDU pdu;
        try {
            byte[] decrypted = servercipher.decrypt(encrypted);
            pdu = PDU.parsedFromPacket(decrypted);
            gk.receivePDU(socket, pdu);
        }
        catch (Exception e) {
            logger.warning(e.toString());
            socket.close();
        }
    }

}
