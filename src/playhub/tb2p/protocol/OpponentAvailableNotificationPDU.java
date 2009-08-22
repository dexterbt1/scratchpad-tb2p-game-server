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
public class OpponentAvailableNotificationPDU extends PDU {

    public static final String COMMAND = "OPPONENT_AVAILABLE";
    
    private String opponentName;

    public OpponentAvailableNotificationPDU(long id, String opponentName) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
        this.setOpponentName(opponentName);
    }

    public String getOpponentName() { return this.opponentName; }
    public void setOpponentName(String name) {
        this.opponentName = name;
        this.json.put("opponent_name", name);
    }

    public OpponentAvailableNotificationPDU(PDU pdu) throws MalformedPDUException {
        if (     (pdu.getCommand().equals(COMMAND))
              && (pdu.getType() == PDU.Type.NOTIFICATION)
              && (pdu.json.containsKey("opponent_name"))
                )
        {
            try {
                this.setCommand(pdu.getCommand());
                this.setId(pdu.getId());
                this.setType(pdu.getType());
                this.setOpponentName(pdu.getPduFieldString("opponent_name"));
            }
            catch (Exception e) {
                throw new MalformedPDUException("Error in parsing OpponentAvailableNotificationPDU");
            }
        }
        else {
            throw new MalformedPDUException("Invalid OpponentAvailableNotificationPDU");
        }
    }


}
