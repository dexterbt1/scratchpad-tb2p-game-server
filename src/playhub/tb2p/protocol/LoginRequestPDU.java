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

    private String gameId;
    private String playerName;
    private BigDecimal betAmount;

    public LoginRequestPDU(String gameId, Player p) {
        this.gameId = gameId;
        this.playerName = p.getName();
        this.betAmount = p.getBetAmount();
    }

    public String getGameId() { return this.gameId; }
    public String getPlayerName() { return this.playerName; }
    public BigDecimal getBetAmount() { return this.betAmount; }


    public LoginRequestPDU(PDU pdu) throws InvalidLoginException {
        boolean successParse = false;
        String gid = null;
        String pname = null;
        BigDecimal bet = null;
        try {
            String req = pdu.getPduFieldString("request");
            if (req.equalsIgnoreCase("LOGIN")) {
                gid = pdu.getPduFieldString("game_id");
                pname = pdu.getPduFieldString("player_name");
                bet = new BigDecimal(pdu.getPduFieldString("bet_amount"));
                successParse = true;
            }
        }
        catch (Exception e) {
            throw new InvalidLoginException("Login PDU parsing error");
        }
        finally {
            if (successParse) {
                this.playerName = pname;
                this.gameId = gid;
                this.betAmount = bet;
            }
            else {
                throw new InvalidLoginException("Login PDU expected");
            }
        }
    }

}
