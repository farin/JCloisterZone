package com.jcloisterzone.game.capability;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;


public final class BarnCapability extends Capability {

    public BarnCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        /*if (game.hasCapability(Capability.FARM_PLACEMENT)) {
            player.addMeeple(new Barn(game, player));
        }*/
        player.addMeeple(new Barn(game, player));
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        Position pos = getTile().getPosition();

        if (game.getActivePlayer().hasSpecialMeeple(Barn.class)) {
            BarnAction barnAction = null;
            Location corner = Location.WR.union(Location.NL);
            Location positionChange = Location.W;
            for (int i = 0; i < 4; i++) {
                if (isBarnCorner(corner, positionChange)) {
                    if (barnAction == null) {
                        barnAction = new BarnAction();
                        actions.add(barnAction);
                    }
                    barnAction.getOrCreate(pos).add(corner);
                }
                corner = corner.next();
                positionChange = positionChange.next();
            }
        }
    }

    private boolean isBarnCorner(Location corner, Location positionChange) {
        Farm farm = null;
        Position pos = getTile().getPosition();
        for (int i = 0; i < 4; i++) {
            Tile tile = getBoard().get(pos);
            if (tile == null) return false;
            farm = (Farm) tile.getFeaturePartOf(corner);
            if (farm == null) return false;
            corner = corner.next();
            pos = pos.add(positionChange);
            positionChange = positionChange.next();
        }

        if (!game.hasRule(CustomRule.MULTI_BARN_ALLOWED)) {
            return !farm.walk(new IsOccupied().with(Barn.class));
        }

        return true;
    }
}