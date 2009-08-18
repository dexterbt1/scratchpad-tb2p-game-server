/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import playhub.tb2p.exceptions.*;
/**
 *
 * @author dexter
 */
public class GameDoneNotificationPDU extends PDU {

    public static final String COMMAND = "GAME_DONE";

    private boolean won;

    public GameDoneNotificationPDU(long id, boolean won) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
        this.setWon(won);
    }

    public boolean wasWon() { return this.won; }
    public void setWon(boolean won) {
        this.won = won;
        this.setPduField("won", Boolean.valueOf(won));
    }

    public GameDoneNotificationPDU(PDU pdu) throws MalformedPDUException {
        if (     (pdu.getCommand().equals(COMMAND))
              && (pdu.getType()==PDU.Type.NOTIFICATION)
              && (pdu.json.containsKey("won")))
        {
            this.setCommand(COMMAND);
            this.setId(pdu.getId());
            this.setType(pdu.getType());
            try {
                Boolean bwon = (Boolean)pdu.getPduField("won");
                this.setWon(bwon.booleanValue());
            }
            catch (Exception e) {
                throw new MalformedPDUException("Invalid GameDoneNotification, unparsable 'won' field.");
            }
        }
        else {
            throw new MalformedPDUException("Invalid GameDoneNotification");
        }
    }

    
}
