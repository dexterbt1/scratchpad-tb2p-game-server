/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.io.Serializable;

/**
 *
 * @author dexter
 */
public class ScoreLogEntry implements Serializable {

    public final static long serialVersionUID = 1;

    public String gameId;
    public String gameName;
    public String playerName;
    public boolean playerWon;
    public long playerScore;
    public boolean statusProperWin;
    public String platform;

    public ScoreLogEntry(String gameId, String gameName, String playerName, boolean playerWon, long playerScore, boolean statusProperWin, String platform) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.playerName = playerName;
        this.playerWon = playerWon;
        this.playerScore = playerScore;
        this.statusProperWin = statusProperWin;
        this.platform = platform;
    }

    @Override
    public String toString() {
        return String.format("ScoreLogEntry: gameid=%s, gamename=%s, player=%s, result=%s, score=%s, status=%s, platform=%s",
                this.gameId,
                this.gameName,
                this.playerName,
                (this.playerWon ? "Won" : "Lost"),
                this.playerScore,
                (this.statusProperWin ? "Proper Win" : "Disconnect"),
                this.platform
                );
    }



}
