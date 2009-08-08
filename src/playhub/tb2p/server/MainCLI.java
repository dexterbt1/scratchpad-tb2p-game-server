/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.Properties;
import java.io.*;
/**
 *
 * @author dexter
 */
public class MainCLI {

    public static final String CONFIG_KEY = "playhub.server.configfile";
    public static final String CLI_USAGE  = "System property '"+CONFIG_KEY+"' not defined. Please set this to the configuration filename. (e.g. java -D"+CONFIG_KEY+"=myserver.properties ...)";

    public static void die(String message) {
        System.err.println(message);
        System.exit(255);
    }

    public static void main(String[] args) {
        String configfile = System.getProperty(MainCLI.CONFIG_KEY);
        if (configfile == null) {
            MainCLI.die(MainCLI.CLI_USAGE);
        }
        
        // load configuration
        Properties c = new Properties();
        try {
            c.load(new FileInputStream(configfile));
        }
        catch (IOException ioe) {
            MainCLI.die("IO-exception occured when loading ["+configfile+"]: "+ioe.toString());
        }

        Server server = new Server(ServerSettings.getInstance(c));
        server.run();
        
    }

}
