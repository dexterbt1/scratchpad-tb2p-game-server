/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import java.math.BigDecimal;
import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class LoginRequestPDU extends PDU {

    public static final String COMMAND = "LOGIN";

    private String gameId;
    private String gameName;
    private String playerName;
    private BigDecimal betAmount;

    public LoginRequestPDU(long id, String gameId, String gameName, Player p) {
        this.setId(id);
        this.setCommand(COMMAND);
        this.setType(Type.REQUEST);
        this.setGameId(gameId);
        this.setPlayerName(p.getName());
        this.setBetAmount(p.getBetAmount());
        this.setGameName(gameName);
    }

    public String getGameId() { return this.gameId; }
    public String getGameName() { return this.gameName; }
    public String getPlayerName() { return this.playerName; }
    public BigDecimal getBetAmount() { return this.betAmount; }

    public void setGameId(String gameId) { 
        this.gameId = gameId;
        this.json.put("game_id", gameId);
    }

    public void setPlayerName(String playerName) { 
        this.playerName = playerName;
        this.json.put("player_name", playerName);
    }

    public void setBetAmount(BigDecimal betAmount) { 
        this.betAmount = betAmount;
        this.json.put("bet_amount", betAmount.toPlainString());
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
        this.json.put( "game_name", gameName );
    }


    public LoginRequestPDU(PDU pdu) throws InvalidLoginException {
        boolean successParse = false;
        String gid = null;
        String gameName = null;
        String pname = null;
        BigDecimal bet = null;
        try {
            if (pdu.getCommand().equals(LoginRequestPDU.COMMAND)) {
                gid = pdu.getPduFieldString("game_id");
                gameName = pdu.getPduFieldString("game_name");
                pname = pdu.getPduFieldString("player_name");
                bet = new BigDecimal(pdu.getPduFieldString("bet_amount"));
                successParse = (gid != null) && (pname != null) && (bet != null);
            }
        }
        catch (Exception e) {
            throw new InvalidLoginException("Login PDU parsing error");
        }
        finally {
            if (successParse) {
                this.setPlayerName(pname);
                this.setGameId(gid);
                this.setGameName(gameName);
                this.setBetAmount(bet);
            }
            else {
                throw new InvalidLoginException("Login PDU expected");
            }
        }
    }

}
