package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.ui.GameController;


public class DragonMovePhase extends ServerAwarePhase {

    private final DragonCapability dragonCap;

    public DragonMovePhase(Game game, GameController controller) {
        super(game, controller);
        dragonCap = game.getCapability(DragonCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(DragonCapability.class);
    }

    @Override
    public Player getActivePlayer() {
        return dragonCap.getDragonPlayer();
    }

    @Override
    public void enter() {
        selectDragonMove();
    }

    private void selectDragonMove() {
        if (dragonCap.getDragonMovesLeft() > 0) {
            Set<Position> moves = dragonCap.getAvailDragonMoves();
            if (!moves.isEmpty()) {
                toggleClock(getActivePlayer());
                game.post(new SelectDragonMoveEvent(getActivePlayer(), moves, dragonCap.getDragonMovesLeft()));
                return;
            }
        }
        dragonCap.endDragonMove();
        next();
    }

    @Override
    public void moveNeutralFigure(FeaturePointer fp, Class<? extends NeutralFigure> figureType) {
        if (Dragon.class.equals(figureType)) {
            assert fp.getLocation() == null;
            Position p = fp.getPosition();
            if (!dragonCap.getAvailDragonMoves().contains(p)) {
                throw new IllegalArgumentException("Invalid dragon move.");
            }
            dragonCap.moveDragon(p);
            for (Meeple m : game.getDeployedMeeples()) {
                if (m.at(p) && m.canBeEatenByDragon()) {
                    m.undeploy();
                }
            }
            selectDragonMove();
        } else {
            super.moveNeutralFigure(fp, figureType);
        }
    }
}
