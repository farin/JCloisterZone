package com.jcloisterzone.game;

import java.io.Serializable;

import com.jcloisterzone.ui.PlayerColors;

/**
 * Represents a slot for a player. Slots can be "open" if no player is in the slot, "own" if the slot is used by the
 * player running the application or "remote" if an artificial intelligence or a remote player is in control of the
 * slot.
 */
public class PlayerSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The state of the slot.
     */
    public enum SlotState {
        /**
         * Open if no one is in control of the slot.
         */
        OPEN,

        /**
         * Own if the player running this application is in control.
         */
        OWN,

        /**
         * Remote if an AI or a remote player is in control.
         */
        REMOTE
    }

    /**
     * The number of slots available (Carcassonne allows up to six)
     */
    public static final int COUNT = 6;

    private final int number;
    private Integer serial; //server assign sequence number when type is occupied

    private String sessionId;
    private String clientId;
    private String nickname;
    private SlotState state = SlotState.OPEN;
    private String aiClassName;

    private transient PlayerColors colors;
    //private transient AiPlayer aiPlayer; //ai player instance, set only on owner host

    /**
     * Instantiates a new slot.
     *
     * @param number the number of this slot
     */
    public PlayerSlot(int number) {
        this.number = number;
    }

    /**
     * Checks if this slot is occupied (i.e., controlled by someone)
     *
     * @return {@code true} if this slot is occupied, {@code false} otherwise
     */
    public boolean isOccupied() {
        return state != SlotState.OPEN;
    }

    /**
     * Checks if this slot is controlled by an artificial intelligence
     *
     * @return {@code true} if this slot is occupied by an artificial intelligence, {@code false} otherwise
     */
    public boolean isAi() {
        return aiClassName != null;
    }

    /**
     * Checks if this slot is controlled by the user of this application
     *
     * @return {@code true} if this slot is occupied by the user of this application, {@code false} otherwise
     */
    public boolean isOwn() {
        return state == SlotState.OWN;
    }

    /**
     * Gets the number of this slot.
     *
     * @return the number of this slot
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the server-assigned serial of this slot.
     *
     * @return the serial of this slot
     */
    public Integer getSerial() {
        return serial;
    }

    /**
     * Sets the serial of this slot.
     *
     * @param serial the serial to set
     */
    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    /**
     * Gets the nickname of the player associated to this slot.
     *
     * @return the nickname of the player associated to this slot
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets nickname of the player associated to this slot.
     *
     * @param nickname the nickname of the player associated to this slot
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets session id of the slot.
     *
     * @return the session id of the slot
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session id of the slot.
     *
     * @param sessionId the session id to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the state of the slot.
     *
     * @return the state of the slot
     */
    public SlotState getState() {
        return state;
    }

    /**
     * Sets the state of the slot.
     *
     * @param state the state of the slot
     */
    public void setState(SlotState state) {
        this.state = state;
    }

    /**
     * Gets the class name of the AI controlling this slot.
     *
     * @return the class name of the AI controlling this slot
     */
    public String getAiClassName() {
        return aiClassName;
    }

    /**
     * Sets the class name of the AI controlling this slot.
     *
     * @param aiClassName the class name of the AI controlling this slot
     */
    public void setAiClassName(String aiClassName) {
        this.aiClassName = aiClassName;
    }

    /**
     * Gets the colors associated with this slot.
     *
     * @return the colors associated with this slot
     */
    public PlayerColors getColors() {
        return colors;
    }

    /**
     * Sets the colors associated with this slot.
     *
     * @param colors the colors associated with this slot
     */
    public void setColors(PlayerColors colors) {
        this.colors = colors;
    }

    @Override
    public String toString() {
        return "("+ number + ") " + state + (nickname == null ? "" : " " + nickname);
    }

    /**
     * Checks whether this slot is connected.
     *
     * @return {@code true} if this slot is disconnected, {@code false} otherwise
     */
    public boolean isDisconnected() {
        return sessionId == null;
    }


//    public AiPlayer getAiPlayer() {
//        return aiPlayer;
//    }
//
//    public void setAiPlayer(AiPlayer aiPlayer) {
//        this.aiPlayer = aiPlayer;
//    }

    /**
     * Gets the client id.
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client id.
     *
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
