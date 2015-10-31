package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.WarningEvent;
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
                if (abbeyCap.hasLastAbbeyChance()) {
                	game.post(new WarningEvent(getActivePlayer()));
                }
                game.post(new SelectActionEvent(getActivePlayer(), new AbbeyPlacementAction().addAll(getBoard().getHoles()), true));
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

        game.post(new TileEvent(TileEvent.PLACEMENT, getActivePlayer(), nextTile, position));
        next(ActionPhase.class);
    }
}
