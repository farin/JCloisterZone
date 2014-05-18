package com.jcloisterzone.ai;

import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;

class SelectDragonMoveTask implements Runnable {

    private final RankingAiPlayer aiPlayer;
    private SelectDragonMoveEvent ev;

    public SelectDragonMoveTask(RankingAiPlayer aiPlayer, SelectDragonMoveEvent ev) {
        this.aiPlayer = aiPlayer;
        this.ev = ev;
    }

    @Override
    public void run() {
        try {
            //TODO
            throw new UnsupportedOperationException("use task from legacy ai player");
        } catch (Exception e) {
             aiPlayer.handleRuntimeError(e);
             aiPlayer.selectDummyDragonMove(ev.getPositions(), ev.getMovesLeft());
        }
    }

//  //@Override
//    public void selectDragonMove(Game game, Set<Position> positions, int movesLeft) {
//        initVars(game);
//        Position dragonPosition = game.getCapability(DragonCapability.class).getDragonPosition();
//        double tensionX = 0, tensionY = 0;
//
//        for (Meeple m : game.getDeployedMeeples()) {
//            int distance = dragonPosition.squareDistance(m.getPosition());
//            if (distance == 0 || distance > movesLeft) continue;
//            if (m.getFeature() instanceof Castle) continue;
//
//            double weight = 1.0 / (distance * distance);
//            if (isMe(m.getPlayer())) {
//                weight *= -0.8;	//co takhle 0.8
//            }
//            tensionX += weight * (m.getPosition().x - dragonPosition.x);
//            tensionY += weight * (m.getPosition().y - dragonPosition.y);
//        }
//
//        double minDiff = Double.MAX_VALUE;
//        Position result = null;
//        for (Position p : positions) {
//            double diff =
//                Math.abs(p.x - dragonPosition.x - tensionX) + Math.abs(p.y - dragonPosition.y - tensionY);
//            if (diff < minDiff) {
//                minDiff = diff;
//                result = p;
//            }
//        }
//        logger.info("Selected dragon move: {}", result);
//        getServer().moveDragon(result);
//    }
}