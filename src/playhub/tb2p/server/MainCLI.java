/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;
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

        //get the top Logger:
        Logger topLogger = java.util.logging.Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        //set the console handler to fine:
        consoleHandler.setLevel(java.util.logging.Level.FINEST);
        consoleHandler.setFormatter(new SingleLineFormatter());



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


    public static final class SingleLineFormatter extends SimpleFormatter {
        @Override
        public String format(LogRecord record) {
            return new java.util.Date() + " " + record.getLevel() + " " + record.getSourceClassName() + " " + record.getSourceMethodName() + " - " + record.getMessage() + "\r\n";
        }
    }


}
