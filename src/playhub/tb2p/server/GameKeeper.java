/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.*;
import java.util.concurrent.*;
import naga.*;

import playhub.tb2p.protocol.*;
/**
 *
 * @author dexter
 */
public class GameKeeper {

    private Map<NIOSocket,GameSession> mapSockGame = new ConcurrentHashMap();
    private Set<GameSession> gamesWaitingPlayers = new HashSet();
    private Set<GameSession> gamesActive = new HashSet();

    public GameKeeper() {

    }

    public void registerSocket(NIOSocket nios) {
        mapSockGame.put(nios, null);
    }

    public void unregisterSocket(NIOSocket nios) {
        mapSockGame.remove(nios);
    }

    public void submit(NIOSocket nios, PDU pdu) {
        GameSession gs = mapSockGame.get(nios);
        if (gs==null) {
            // meaning the player is new, so we require a login first
            //PDU response_pdu = gs.login(pdu);
            //
        }
    }

}
