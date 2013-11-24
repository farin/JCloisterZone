package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class PigCapability extends Capability {

    public PigCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Pig(game, player));
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        Player player = game.getActivePlayer();
        if (!player.hasSpecialMeeple(Pig.class)) return;

        Tile tile = getTile();
        if (!game.isDeployAllowed(tile, Pig.class)) return;

        Position pos = tile.getPosition();
        Set<Location> locations = tile.getPlayerFeatures(player, Farm.class);
        if (!locations.isEmpty()) {
            MeepleAction pigAction = new MeepleAction(Pig.class);
            pigAction.getOrCreate(pos).addAll(locations);
            actions.add(pigAction);
        }
    }

}
