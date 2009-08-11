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
public class Server {

    protected ServerSettings settings;
    protected Logger logger = Logger.getLogger("playhub.tb2p.server.Server");

    public Server(ServerSettings settings) {
        this.settings = settings;
    }

    public void run() {
        ClientManager cso = new ClientManager(settings);
        ServerSocketHandler ssh;
        try {
            NIOService service = new NIOService();
            NIOServerSocket socket = service.openServerSocket(settings.getPort());

            ssh = new ServerSocketHandler(socket, cso);
            socket.listen(ssh);

            logger.info("server ready to accept connections.");
            while (true) {
                service.selectBlocking();
            }
        }
        catch (IOException ioe) {
            logger.severe(ioe.toString());
        }

        
    }

}
