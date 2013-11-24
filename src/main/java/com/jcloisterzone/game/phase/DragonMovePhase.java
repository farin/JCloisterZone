package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;


public class DragonMovePhase extends Phase {

    private final DragonCapability dragonCap;

    public DragonMovePhase(Game game) {
        super(game);
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
                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectDragonMove(moves, dragonCap.getDragonMovesLeft());
                return;
            }
        }
        dragonCap.endDragonMove();
        next();
    }

    @Override
    public void moveDragon(Position p) {
        if (!dragonCap.getAvailDragonMoves().contains(p)) {
            throw new IllegalArgumentException("Invalid dragon move.");
        }
        dragonCap.moveDragon(p);
        for (Meeple m : game.getDeployedMeeples()) {
            if (m.at(p) && m.canBeEatenByDragon()) {
                m.undeploy();
            }
        }
        game.fireGameEvent().dragonMoved(p);
        selectDragonMove();
    }

}
