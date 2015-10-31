package com.jcloisterzone.event;

import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;

@Idempotent
public class SelectActionEvent extends PlayEvent {

    private final boolean passAllowed;
    private final List<? extends PlayerAction<?>> actions;


    public SelectActionEvent(Player targetPlayer, List<? extends PlayerAction<?>> actions, boolean passAllowed) {
        super(null, targetPlayer);
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public SelectActionEvent(Player player, PlayerAction<?> action, boolean passAllowed) {
        this(player, Collections.<PlayerAction<?>>singletonList(action), passAllowed);
    }

    public List<? extends PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }

    @Override
    public String toString() {
        return super.toString() + " passAllowed:" + passAllowed + " actions:" + actions;
    }
}
