package com.jcloisterzone.action;

import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public abstract class SelectTileAction extends PlayerAction {

    private final Set<Position> sites;

    public SelectTileAction(String name) {
        super(name);
        this.sites = new HashSet<>();
    }

    public SelectTileAction(String name, Set<Position> sites) {
        super(name);
        this.sites = sites;
    }

    public Set<Position> getSites() {
        return sites;
    }

    public abstract void perform(Client2ClientIF server, Position p);
}
