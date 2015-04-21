package com.jcloisterzone.event;

import com.jcloisterzone.Player;

/**
 * Ancestor for all in-game event.
 */
public abstract class PlayEvent extends Event {

    private final Player triggeringPlayer;
    private final Player targetPlayer;

    public PlayEvent(Player triggeringPlayer, Player targetPlayer) {
        this(0, triggeringPlayer, targetPlayer);
    }

    public PlayEvent(int type, Player triggeringPlayer, Player targetPlayer) {
        super(type);
        this.triggeringPlayer = triggeringPlayer;
        this.targetPlayer = targetPlayer;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public Player getTriggeringPlayer() {
        return triggeringPlayer;
    }
}
