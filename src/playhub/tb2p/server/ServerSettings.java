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
    private int playTurnDurationSeconds;
    private int clientWaitTimeoutSeconds;

    public ServerSettings() {
    }

    public int getPort() { return this.port; }
    public void setPort(int port) { this.port = port; }

    public int getMaxConnections() { return this.maxConnections; }
    public void setMaxConnections(int maxPlayers) { this.maxConnections = maxPlayers; }

    public int getPlayTurnDurationSeconds() { return this.playTurnDurationSeconds; }
    public void setPlayTurnDurationSeconds(int s) { this.playTurnDurationSeconds = s; }

    public int getClientWaitTimeoutSeconds() { return this.clientWaitTimeoutSeconds; }
    public void setClientWaitTimeoutSeconds(int s) { this.clientWaitTimeoutSeconds = s; }

    public static ServerSettings getInstance(Properties prop) {
        ServerSettings settings = new ServerSettings();
        // server.port
        int port = Integer.parseInt(prop.getProperty("server.port"));
        settings.setPort(port);
        // server.max_connections
        int max_connections = Integer.parseInt(prop.getProperty("server.max_connections"));
        settings.setMaxConnections(max_connections);
        // server.play_turn_duration_seconds
        int play_turn_duration = Integer.parseInt(prop.getProperty("server.play_turn_duration_seconds"));
        settings.setPlayTurnDurationSeconds(play_turn_duration);
        // server.play_turn_duration_seconds
        int client_wait = Integer.parseInt(prop.getProperty("server.client_wait_timeout_seconds"));
        settings.setClientWaitTimeoutSeconds(client_wait);

        return settings;
    }

}
