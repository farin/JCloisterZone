package com.jcloisterzone.game.capability;

import java.util.List;

import org.w3c.dom.Element;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class FestivalCapability extends Capability {

    public FestivalCapability(Game game) {
        super(game);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("festival").getLength() > 0) {
            tile.setTrigger(TileTrigger.FESTIVAL);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        if (!getTile().hasTrigger(TileTrigger.FESTIVAL)) return;

        Player activePlayer = game.getActivePlayer();
        UndeployAction action = new UndeployAction("festival", PlayerRestriction.only(activePlayer));

        for (Meeple m : Iterables.filter(activePlayer.getMeeples(), MeeplePredicates.deployed())) {
            action.getOrCreate(m.getPosition()).add(m.getLocation());
        }
        if (!action.getLocationsMap().isEmpty()) {
            actions.add(action);
        }
    }



}
