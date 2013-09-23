package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;


public class DragonMovePhase extends Phase {

    final DragonCapability dgCap;

    public DragonMovePhase(Game game) {
        super(game);
        dgCap = game.getDragonCapability();
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(Capability.DRAGON);
    }

    @Override
    public Player getActivePlayer() {
        return dgCap.getDragonPlayer();
    }

    @Override
    public void enter() {
        selectDragonMove();
    }

    private void selectDragonMove() {
        if (dgCap.getDragonMovesLeft() > 0) {
            Set<Position> moves = dgCap.getAvailDragonMoves();
            if (!moves.isEmpty()) {
                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectDragonMove(moves, dgCap.getDragonMovesLeft());
                return;
            }
        }
        dgCap.endDragonMove();
        next();
    }

    @Override
    public void moveDragon(Position p) {
        if (!dgCap.getAvailDragonMoves().contains(p)) {
            throw new IllegalArgumentException("Invalid dragon move.");
        }
        dgCap.moveDragon(p);
        for (Meeple m : game.getDeployedMeeples()) {
            if (m.getPosition().equals(p) && m.canBeEatenByDragon()) {
                m.undeploy();
            }
        }
        game.fireGameEvent().dragonMoved(p);
        selectDragonMove();
    }

}
