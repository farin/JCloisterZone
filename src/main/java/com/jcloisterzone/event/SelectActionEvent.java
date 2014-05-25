package com.jcloisterzone.event;

import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;

public class SelectActionEvent extends PlayEvent {

    private final boolean passAllowed;
    private final List<? extends PlayerAction<?>> actions;


    public SelectActionEvent(Player player, List<? extends PlayerAction<?>> actions, boolean passAllowed) {
        super(player);
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
}
