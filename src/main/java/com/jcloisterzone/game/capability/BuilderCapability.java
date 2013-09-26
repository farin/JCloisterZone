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
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Capability;

public class BuilderCapability extends Capability {

    public enum BuilderState { INACTIVE, ACTIVATED, BUILDER_TURN; }

    protected BuilderState builderState = BuilderState.INACTIVE;

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Builder(game, player));
    }

    public BuilderState getBuilderState() {
        return builderState;
    }

    public void builderUsed() {
        if (builderState == BuilderState.INACTIVE) {
            builderState = BuilderState.ACTIVATED;
        }
    }

    public boolean hasPlayerAnotherTurn() {
        return builderState == BuilderState.ACTIVATED;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        if (getTile().getTrigger() == TileTrigger.VOLCANO &&
            getGame().hasRule(CustomRule.CANNOT_PLACE_BUILDER_ON_VOLCANO)) return;

        Player player = game.getActivePlayer();
        Position pos = getTile().getPosition();
        if (player.hasSpecialMeeple(Builder.class)) {
            MeepleAction meepleAction = new MeepleAction(Builder.class);
            Set<Location> dirs = getTile().getPlayerFeatures(player, Road.class);
            if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
            dirs = getTile().getPlayerFeatures(player, City.class);
            if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
            if (! meepleAction.getSites().isEmpty()) actions.add(meepleAction);
        }
    }

    @Override
    public void turnCleanUp() {
        if (builderState == BuilderState.ACTIVATED) {
            builderState = BuilderState.BUILDER_TURN;
            return;
        }
        if (builderState == BuilderState.BUILDER_TURN) {
            builderState = BuilderState.INACTIVE;
        }
    }

    @Override
    public BuilderCapability copy() {
        BuilderCapability copy = new BuilderCapability();
        copy.game = game;
        copy.builderState = builderState;
        return copy;
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
