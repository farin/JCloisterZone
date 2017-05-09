package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;

public class FestivalCapability extends Capability<Void> {

    public final String UNDEPLOY_FESTIVAL = "festival";


    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("festival").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.FESTIVAL);
        }
        return tile;
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (!getCurrentTile().hasTrigger(TileTrigger.FESTIVAL)) return;

        Player activePlayer = game.getActivePlayer();
        UndeployAction action = new UndeployAction(UNDEPLOY_FESTIVAL);

        for (Meeple m : Iterables.filter(activePlayer.getMeeples(), MeeplePredicates.deployed())) {
            action.add(new MeeplePointer(m));
        }
        if (!action.isEmpty()) {
            actions.add(action);
        }
    }



}
