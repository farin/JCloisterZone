package com.jcloisterzone.event;

import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;

public class SelectActionEvent extends Event {

    private final boolean passAllowed;
    private final List<PlayerAction> actions;


    public SelectActionEvent(Player player, List<PlayerAction> actions, boolean passAllowed) {
        super(player);
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public SelectActionEvent(Player player, PlayerAction action, boolean passAllowed) {
        this(player, Collections.singletonList(action), passAllowed);
    }

    public List<PlayerAction> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
}
