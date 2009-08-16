/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import java.util.logging.*;
import org.json.simple.*;
import org.json.simple.parser.*;

import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class PDU {

    public static enum Type {
        REQUEST,
        RESPONSE,
        NOTIFICATION,
    }

    private Type type;
    private long id;
    private String command;

    protected JSONObject json;

    public PDU() {
        this.json = new JSONObject();
    }

    public PDU(JSONObject jo) {
        this.json = jo;
    }

    public Type getType() { return this.type; }
    public void setType(Type type) { this.type = type; this.json.put("type", type.toString()); }

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; this.json.put("id", Long.valueOf(id)); }

    public String getCommand() { return this.command; }
    public void setCommand(String command) { this.command = command; this.json.put("command", command); }

    public Object getPduField(String key) {
        return json.get(key);
    }

    public String getPduFieldString(String key) {
        Object o = this.getPduField(key);
        String v = (String)o;
        return v;
    }

    public void setPduField(String key, Object o) {
        this.json.put(key, o);
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
            pdu.setType(Type.valueOf((String)jo.get("type")));
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

        // parse command (optional)
        if (jo.containsKey("command")) {
            Object command = jo.get("command");
            if ((command != null) && (command instanceof String)) {
                pdu.setCommand((String)command);
            }
        }

        return pdu;
    }


    public String toJSONString() {
        final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
        String r = this.json.toJSONString();
        logger.finest(r);
        return r;
    }


    @Override
    public String toString() {
        return this.toJSONString();
    }

}

