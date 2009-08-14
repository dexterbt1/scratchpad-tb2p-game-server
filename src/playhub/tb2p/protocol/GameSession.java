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
public class GameSession {

    public static enum State {
        CREATED,
        WAIT_PLAYER2,
        PLAY_PLAYER1,
        PLAY_PLAYER2,
        DONE,
        ABORTED
    }

    private State gameState;
    private String gameId, gameIdLowercase;
    private Player player1, player2;
    private boolean player1TurnStarted, player2TurnStarted;
    private boolean player1TurnCompleted, player2TurnCompleted;

    public GameSession(String gameId) {
        this.gameId = gameId;
        this.gameIdLowercase = gameId.toLowerCase();
        this.gameState = State.CREATED;
        this.player1TurnCompleted = false;
        this.player2TurnCompleted = false;
        this.player1TurnStarted = false;
        this.player2TurnStarted = false;
    }

    @Override
    public int hashCode() { return this.gameIdLowercase.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GameSession other = (GameSession) obj;
        if ((this.getGameId() == null) ? (other.getGameId() != null) : !this.getGameId().equals(other.getGameId())) {
            return false;
        }
        return true;
    }

    public State getGameState() { return this.gameState; }
    public String getGameId() { return this.gameId; }
    public Player getPlayer1() { return this.player1; }
    public Player getPlayer2() { return this.player2; }

    public boolean hasPlayer1TurnStarted() { return this.player1TurnStarted; }
    public boolean hasPlayer1TurnCompleted() { return this.player1TurnCompleted; }

    public boolean hasPlayer2TurnStarted() { return this.player2TurnStarted; }
    public boolean hasPlayer2TurnCompleted() { return this.player2TurnCompleted; }



    
    public void loginPlayer1(Player p1) throws GameStateViolation {
        if (this.getGameState() == State.CREATED) {
            this.player1 = p1;
            this.gameState = State.WAIT_PLAYER2;
        }
        else {
            throw new GameStateViolation();
        }
    }

    public void loginPlayer2(Player p2) throws GameStateViolation {
        if (this.getGameState() == State.WAIT_PLAYER2) {
            this.player2 = p2;
            this.gameState = State.PLAY_PLAYER1;
        }
        else {
            throw new GameStateViolation();
        }
    }

    public void startPlayPlayer1() throws GameStateViolation {
        if ( (this.getGameState() == State.PLAY_PLAYER1) && 
             (!this.hasPlayer1TurnStarted()) &&
             (!this.hasPlayer1TurnCompleted()) ) {
            this.player1TurnStarted = true;
        }
        else {
            throw new GameStateViolation();
        }
    }

    public void endPlayPlayer1() throws GameStateViolation {
        if ( (this.getGameState() == State.PLAY_PLAYER1) &&
             (this.hasPlayer1TurnStarted()) &&
             (!this.hasPlayer1TurnCompleted()) ) {
            this.player1TurnCompleted = true;
            this.gameState = State.PLAY_PLAYER2;
        }
        else {
            throw new GameStateViolation();
        }
    }

    public void startPlayPlayer2() throws GameStateViolation {
        if ( (this.getGameState() == State.PLAY_PLAYER2) &&
             (!this.hasPlayer2TurnStarted()) &&
             (!this.hasPlayer2TurnCompleted()) ) {
            this.player2TurnStarted = true;

        }
        else {
            throw new GameStateViolation();
        }
    }

    public void endPlayPlayer2() throws GameStateViolation {
        if ( (this.getGameState() == State.PLAY_PLAYER2) &&
             (this.hasPlayer2TurnStarted()) &&
             (!this.hasPlayer2TurnCompleted()) ) {
            this.player2TurnCompleted = true;
            this.gameState = State.DONE;
        }
        else {
            throw new GameStateViolation();
        }
    }


    
}
