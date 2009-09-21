/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.gaborcselle.persistent.*;

import org.apache.http.*;
import org.apache.http.protocol.*;
import org.apache.http.message.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.utils.*;

/**
 *
 * @author dexter
 */
public class ScoreLogReporter implements Runnable {

    private final Logger logger = Logger.getLogger(ScoreLogReporter.class.getCanonicalName());

    private String reportURL;
    private PersistentQueue queue;

    public ScoreLogReporter(PersistentQueue queue, String reportURL) {
        this.queue = queue;
        this.reportURL = reportURL;
    }

    public boolean sendEntry(ScoreLogEntry entry) {
        logger.info(entry.toString());

        HttpClient httpclient = new DefaultHttpClient();

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("gameid", entry.gameId));
        qparams.add(new BasicNameValuePair("gamename", entry.gameName));
        qparams.add(new BasicNameValuePair("player", entry.playerName));
        qparams.add(new BasicNameValuePair("result", entry.playerWon ? "Won" : "Lost") );
        qparams.add(new BasicNameValuePair("score", String.valueOf(entry.playerScore)));
        qparams.add(new BasicNameValuePair("status", entry.statusProperWin ? "Proper Win" : "Disconnect"));
        qparams.add(new BasicNameValuePair("platform", entry.platform));

        String fullURIString = reportURL + "?" + URLEncodedUtils.format(qparams, HTTP.DEFAULT_CONTENT_CHARSET);
        logger.finest("about to request: "+fullURIString);
        HttpGet httpget = new HttpGet(fullURIString);

        // Create a response handler
        try {
            HttpResponse response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (IOException ioe) {
            return false; // error
        }
        finally {
            httpclient.getConnectionManager().shutdown();
        }

    }

    public void run() {
        while (true) {
            // do some work (poll)
            while (queue.size()>0) {
                try {
                    Object o = queue.peek(); // don't remove yet
                    ScoreLogEntry entry = (ScoreLogEntry) o;
                    boolean sent = false;
                    while (!sent) {
                        sent = this.sendEntry(entry);
                    }
                    queue.remove(); // sent already, now we can remove
                }
                catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(255);
                }
            }
            // rest for awhile
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
                System.exit(255);
            }
        }
    }



}
