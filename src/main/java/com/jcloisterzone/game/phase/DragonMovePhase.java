package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.figure.Meeple;
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
    public void moveDragon(Position p) {
        if (!dragonCap.getAvailDragonMoves().contains(p)) {
            throw new IllegalArgumentException("Invalid dragon move.");
        }
        Player player = getActivePlayer();
        Position fromPosition = dragonCap.getDragonPosition();
        dragonCap.moveDragon(p);
        for (Meeple m : game.getDeployedMeeples()) {
            if (m.at(p) && m.canBeEatenByDragon()) {
                m.undeploy();
            }
        }
        game.post(new NeutralFigureMoveEvent(NeutralFigureMoveEvent.DRAGON, player, fromPosition, p));
        selectDragonMove();
    }

}
