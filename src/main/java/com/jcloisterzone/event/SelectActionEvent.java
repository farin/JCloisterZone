package com.jcloisterzone.event;

import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;

@Idempotent
public class SelectActionEvent extends PlayEvent {

    private final boolean passAllowed;
    private final boolean passWarning;
    private final List<? extends PlayerAction<?>> actions;


    public SelectActionEvent(Player targetPlayer, List<? extends PlayerAction<?>> actions, boolean passAllowed, boolean passWarning) {
        super(null, targetPlayer);
        this.actions = actions;
        this.passAllowed = passAllowed;
        this.passWarning = passWarning;
    }

    public SelectActionEvent(Player player, PlayerAction<?> action, boolean passAllowed, boolean passWarning) {
        this(player, Collections.<PlayerAction<?>>singletonList(action), passAllowed, passWarning);
    }

    public List<? extends PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
    
    public boolean hasPassWarning() {
    	return passWarning;
    }

    @Override
    public String toString() {
        return super.toString() + " passAllowed:" + passAllowed + " actions:" + actions;
    }
}
