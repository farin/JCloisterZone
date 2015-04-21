package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
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
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        Player player = game.getActivePlayer();
        if (!player.hasSpecialMeeple(Pig.class)) return;

        Tile tile = getCurrentTile();
        if (!game.isDeployAllowed(tile, Pig.class)) return;

        Position pos = tile.getPosition();
        MeepleAction pigAction = null;
        for (Location loc : tile.getPlayerFeatures(player, Farm.class)) {
            if (pigAction == null) {
                pigAction = new MeepleAction(Pig.class);
                actions.add(pigAction);
            }
            pigAction.add(new FeaturePointer(pos, loc));
        }
    }
}
