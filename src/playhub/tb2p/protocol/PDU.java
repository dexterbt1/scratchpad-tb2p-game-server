/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class PDU {

    private PDUType type;
    private long id;
    private JSONObject json;

    public PDU() {
        this.json = new JSONObject();
    }

    public PDU(JSONObject jo) {
        this.json = jo;
    }

    public PDUType getType() { return this.type; }
    public void setType(PDUType type) { this.type = type; }

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public Object getPduField(String key) {
        return json.get(key);
    }

    public String getPduFieldString(String key) {
        Object o = this.getPduField(key);
        String v = (String)o;
        return v;
    }


    public static PDU parsedFromPacket(byte[] rawpacket) throws MalformedPDUException {
        // assume rawpacket is a json string already
        String json_string = new String(rawpacket);
        // assume all packets is made up of a JSONObject
        JSONParser jp = new JSONParser();
        Object o;
        try {
            o = jp.parse(json_string);
        }
        catch (ParseException pe) {
            throw new MalformedPDUException(pe.toString());
        }
        JSONObject jo = (JSONObject)o;


        PDU pdu = new PDU(jo);

        // parse type
        if (!jo.containsKey("type")) { throw new MalformedPDUException("pdu.type is required"); }
        try {
            pdu.setType(PDUType.valueOf((String)jo.get("type")));
        }
        catch (Exception e) {
            throw new MalformedPDUException("pdu.type is invalid");
        }

        // parse pnum
        if (!jo.containsKey("id")) { throw new MalformedPDUException("pdu.id is required"); }
        try {
            Number n = (Number)jo.get("id");
            pdu.setId(n.longValue());
        }
        catch (Exception e) {
            throw new MalformedPDUException("pdu.id is invalid");
        }

        return pdu;
    }

}

