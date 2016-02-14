package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderCapability.BuilderState;
import com.jcloisterzone.ui.GameController;

public class AbbeyPhase extends ServerAwarePhase {

    private AbbeyCapability abbeyCap;
    private BazaarCapability bazaarCap;
    private BuilderCapability builderCap;

    public AbbeyPhase(Game game, GameController controller) {
        super(game, controller);
        abbeyCap = game.getCapability(AbbeyCapability.class);
        bazaarCap = game.getCapability(BazaarCapability.class);
        builderCap = game.getCapability(BuilderCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(AbbeyCapability.class);
    }

    @Override
    public void enter() {
        boolean baazaarInProgress = bazaarCap != null && bazaarCap.getBazaarSupply() != null;
        boolean builderSecondTurnPart = builderCap != null && builderCap.getBuilderState() == BuilderState.BUILDER_TURN;
        if (builderSecondTurnPart || !baazaarInProgress) {
            if (abbeyCap.hasUnusedAbbey(getActivePlayer()) && !getBoard().getHoles().isEmpty()) {
                toggleClock(getActivePlayer());
                AbbeyPlacementAction action = new AbbeyPlacementAction();
                action.addAll(getBoard().getHoles());
                action.setOccupiedPositions(getBoard().getOccupied());
                game.post(new SelectActionEvent(getActivePlayer(), action, true));
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
    	if (!getBoard().getHoles().contains(position)) {
    		return;
    	}
        abbeyCap.useAbbey(getActivePlayer());

        Tile nextTile = game.getTilePack().drawTile("inactive", Tile.ABBEY_TILE_ID);
        game.setCurrentTile(nextTile);
        nextTile.setRotation(rotation);
        getBoard().add(nextTile, position);
        getBoard().mergeFeatures(nextTile);

        game.post(new TileEvent(TileEvent.PLACEMENT, getActivePlayer(), nextTile, position));
        next(ActionPhase.class);
    }
}
