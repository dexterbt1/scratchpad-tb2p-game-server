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
public class ScoreUpdateNotificationPDU extends PDU {

    public static final String COMMAND = "SCORE_UPDATE";

    private long score;

    public ScoreUpdateNotificationPDU(long id, long score) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
        this.setScore(score);
    }


    public long getScore() { return this.score; }
    public void setScore(long s) { this.score = s; this.setPduField("score", Long.valueOf(s)); }

    public ScoreUpdateNotificationPDU(PDU pdu) throws MalformedPDUException {
        boolean successParse = false;
        if (    (pdu.getCommand().equals(COMMAND))
             && (pdu.getType() == Type.NOTIFICATION)
             && (pdu.json.containsKey("score")))
        {
            this.setCommand(pdu.getCommand());
            this.setId(pdu.getId());
            this.setType(pdu.getType());
            try {
                long sc = ((Number)pdu.getPduField("score")).longValue();
                this.setScore(sc);
            }
            catch (Exception e) {
                throw new MalformedPDUException(e.toString());
            }
        }
        else {
            throw new MalformedPDUException("Invalid PDU format");
        }
    }

}
