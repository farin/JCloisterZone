package com.jcloisterzone.action;

import java.util.Collections;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;

public abstract class SelectFeatureAction extends PlayerAction {

    private final Sites sites;

    public SelectFeatureAction(String name) {
        this(name, new Sites());
    }

    public SelectFeatureAction(String name, Position p, Set<Location> locations) {
        this(name);
        sites.put(p, locations);
    }

    public SelectFeatureAction(String name, Sites sites) {
        super(name);
        this.sites = sites;
    }

    public Sites getSites() {
        return sites;
    }

    public Set<Location> get(Position p) {
        Set<Location> locs = sites.get(p);
        return locs != null ? locs : Collections.<Location>emptySet();
    }

    public Set<Location> getOrCreate(Position p) {
        return sites.getOrCreate(p);
    }

    @Override
    protected GridLayer createGridLayer() {
        return new FeatureAreaLayer(client.getGridPanel(), this);
    }

    public abstract void perform(Client2ClientIF server, Position p, Location loc);

    @Override
    public String toString() {
        return getClass().getSimpleName() + '=' + sites.toString();
    }

}
