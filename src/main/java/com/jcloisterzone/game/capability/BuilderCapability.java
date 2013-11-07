package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BuilderCapability extends Capability {

    public enum BuilderState { INACTIVE, ACTIVATED, BUILDER_TURN; }

    protected BuilderState builderState = BuilderState.INACTIVE;

    public BuilderCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return builderState;
    }

    @Override
    public void restore(Object data) {
        builderState = (BuilderState) data;
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Builder(game, player));
    }

    public BuilderState getBuilderState() {
        return builderState;
    }

    public void useBuilder() {
        if (builderState == BuilderState.INACTIVE) {
            builderState = BuilderState.ACTIVATED;
        }
    }

    public boolean hasPlayerAnotherTurn() {
        return builderState == BuilderState.ACTIVATED;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        Player player = game.getActivePlayer();
        if (!player.hasSpecialMeeple(Builder.class)) return;

        Tile tile = getTile();
        if (!game.isDeployAllowed(tile, Builder.class)) return;

        Set<Location> roads = tile.getPlayerFeatures(player, Road.class);
        Set<Location> cities = tile.getPlayerFeatures(player, City.class);
        if (roads.isEmpty() && cities.isEmpty()) return;

        Position pos = tile.getPosition();
        MeepleAction builderAction = new MeepleAction(Builder.class);

        builderAction.getOrCreate(pos).addAll(roads);
        builderAction.getOrCreate(pos).addAll(cities);
        actions.add(builderAction);

    }

    @Override
    public void turnCleanUp() {
        switch (builderState) {
        case ACTIVATED:
            builderState = BuilderState.BUILDER_TURN;
            break;
        case BUILDER_TURN:
            builderState = BuilderState.INACTIVE;
            break;
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        node.setAttribute("builderState", builderState.name());
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        builderState = BuilderState.valueOf(node.getAttribute("builderState"));
    }

}
