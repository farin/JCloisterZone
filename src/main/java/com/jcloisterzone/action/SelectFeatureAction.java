package com.jcloisterzone.action;

import java.util.Collections;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;

public abstract class SelectFeatureAction extends PlayerAction {

    private final LocationsMap locMap;

    public SelectFeatureAction(String name) {
        this(name, new LocationsMap());
    }

    public SelectFeatureAction(String name, Position p, Set<Location> locations) {
        this(name);
        locMap.put(p, locations);
    }

    public SelectFeatureAction(String name, LocationsMap sites) {
        super(name);
        this.locMap = sites;
    }

    public LocationsMap getLocationsMap() {
        return locMap;
    }

    public Set<Location> get(Position p) {
        Set<Location> locs = locMap.get(p);
        return locs != null ? locs : Collections.<Location>emptySet();
    }

    public Set<Location> getOrCreate(Position p) {
        return locMap.getOrCreate(p);
    }

    @Override
    protected GridLayer createGridLayer() {
        return new FeatureAreaLayer(client.getGridPanel(), this);
    }

    public abstract void perform(Client2ClientIF server, Position p, Location loc);

    @Override
    public String toString() {
        return getClass().getSimpleName() + '=' + locMap.toString();
    }

}
