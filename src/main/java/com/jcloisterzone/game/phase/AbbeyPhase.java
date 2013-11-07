package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;

public class AbbeyPhase extends Phase {

    private AbbeyCapability abbeyCap;
    private BazaarCapability bazaarCap;

    public AbbeyPhase(Game game) {
        super(game);
        abbeyCap = game.getCapability(AbbeyCapability.class);
        bazaarCap = game.getCapability(BazaarCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(AbbeyCapability.class);
    }

    @Override
    public void enter() {
        if (bazaarCap == null || bazaarCap.getBazaarSupply() == null) {
            if (abbeyCap.hasUnusedAbbey(getActivePlayer()) && ! getBoard().getHoles().isEmpty()) {
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
        abbeyCap.useAbbey(getActivePlayer());

        Tile nextTile = game.getTilePack().drawTile("inactive", Tile.ABBEY_TILE_ID);
        game.setCurrentTile(nextTile);
        nextTile.setRotation(rotation);
        getBoard().add(nextTile, position);
        getBoard().mergeFeatures(nextTile);

        game.fireGameEvent().tilePlaced(nextTile);
        next(ActionPhase.class);
    }
}
