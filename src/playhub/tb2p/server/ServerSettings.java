/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.Properties;

/**
 *
 * @author dexter
 */
public class ServerSettings {

    private int port;
    private int maxConnections;

    public ServerSettings() {
    }

    public int getPort() { return this.port; }
    public void setPort(int port) { this.port = port; }

    public int getMaxConnections() { return this.maxConnections; }
    public void setMaxConnections(int maxPlayers) { this.maxConnections = maxPlayers; }

    public static ServerSettings getInstance(Properties prop) {
        ServerSettings settings = new ServerSettings();
        // server.port
        int port = Integer.parseInt(prop.getProperty("server.port"));
        settings.setPort(port);
        // server.max_connections
        int max_connections = Integer.parseInt(prop.getProperty("server.max_connections"));
        settings.setMaxConnections(max_connections);
        return settings;
    }

}
