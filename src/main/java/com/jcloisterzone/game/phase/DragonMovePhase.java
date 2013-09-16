package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;


public class DragonMovePhase extends Phase {

    final PrincessAndDragonGame pdg;

    public DragonMovePhase(Game game) {
        super(game);
        pdg = game.getPrincessAndDragonGame();
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.PRINCESS_AND_DRAGON) && game.hasCapability(Capability.DRAGON);
    }

    @Override
    public Player getActivePlayer() {
        return pdg.getDragonPlayer();
    }

    @Override
    public void enter() {
        selectDragonMove();
    }

    private void selectDragonMove() {
        if (pdg.getDragonMovesLeft() > 0) {
            Set<Position> moves = pdg.getAvailDragonMoves();
            if (!moves.isEmpty()) {

                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectDragonMove(moves, pdg.getDragonMovesLeft());
                return;
            }
        }
        pdg.endDragonMove();
        next();
    }

    @Override
    public void moveDragon(Position p) {
        if (!pdg.getAvailDragonMoves().contains(p)) {
            throw new IllegalArgumentException("Invalid dragon move.");
        }
        pdg.moveDragon(p);
        for (Meeple m : game.getDeployedMeeples()) {
            if (m.getPosition().equals(p) && m.canBeEatenByDragon()) {
                m.undeploy();
            }
        }
        game.fireGameEvent().dragonMoved(p);
        selectDragonMove();
    }

}
