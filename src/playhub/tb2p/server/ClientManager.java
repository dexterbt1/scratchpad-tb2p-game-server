/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;

import naga.*;
import naga.packetreader.*;

import playhub.tb2p.protocol.*;
import playhub.tb2p.exceptions.*;
import playhub.tb2p.server.*;

/**
 *
 * @author dexter
 */
public class ClientManager extends SocketObserverAdapter {

    protected static Logger logger = Logger.getLogger("playhub.tb2p.server.Server");
    private ServerSettings settings;
    private GameKeeper gk = new GameKeeper();

    public ClientManager(ServerSettings settings) {
        this.settings = settings;
    }

    @Override
    public void connectionOpened(NIOSocket socket) {
        // otherwise, we will accept the connection
        logger.info("socket opened: "+socket.getIp()+":"+socket.getPort());
        socket.setPacketReader(new AsciiLinePacketReader());
        gk.registerSocket(socket);
    }

    @Override
    public void connectionBroken(NIOSocket socket, Exception e) {
        logger.info("socket disconnected: "+socket.getIp()+":"+socket.getPort());
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
        System.err.println("got pdu type="+pdu.getType().toString());
        gk.submit(socket, pdu);
    }

}
