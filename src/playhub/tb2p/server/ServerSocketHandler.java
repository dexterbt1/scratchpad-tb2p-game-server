/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import naga.*;
/**
 *
 * @author dexter
 */
public class ServerSocketHandler extends ServerSocketObserverAdapter {

    private ClientSocketHandler cso;
    private NIOServerSocket serverSocket;

    
    public ServerSocketHandler(NIOServerSocket servsock, ClientSocketHandler cso) {
        this.cso = cso;
        this.serverSocket = servsock;
        serverSocket.setConnectionAcceptor(ConnectionAcceptor.ALLOW); // default
    }

    @Override
    public void newConnection(NIOSocket socket) {
        socket.listen(cso);
    }

}
