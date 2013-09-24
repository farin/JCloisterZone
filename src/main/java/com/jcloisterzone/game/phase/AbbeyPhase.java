package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;

public class AbbeyPhase extends Phase {

    public AbbeyPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.ABBEY_AND_MAYOR);
    }

    @Override
    public void enter() {
        AbbeyCapability amGame = game.getAbbeyCapability();
        BazaarCapability bcbGame = game.getBazaarCapability();
        if (bcbGame == null || bcbGame.getBazaarSupply() == null) {
            if (amGame.hasUnusedAbbey(getActivePlayer()) && ! getBoard().getHoles().isEmpty()) {
                notifyUI(new AbbeyPlacementAction(getBoard().getHoles()), true);
                return;
            }
        }
        next();
    }

    @Override
    public void pass() {
        next();
    }

    @Override
    public void placeTile(Rotation rotation, Position position) {
        AbbeyCapability amGame = game.getAbbeyCapability();
        amGame.useAbbey(getActivePlayer());

        Tile nextTile = game.getTilePack().drawTile("inactive", Tile.ABBEY_TILE_ID);
        game.setCurrentTile(nextTile);
        nextTile.setRotation(rotation);
        getBoard().add(nextTile, position);
        getBoard().mergeFeatures(nextTile);

        game.fireGameEvent().tilePlaced(nextTile);
        next(ActionPhase.class);
    }
}
