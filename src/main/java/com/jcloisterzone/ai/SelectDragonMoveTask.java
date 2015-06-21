package com.jcloisterzone.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;

class SelectDragonMoveTask implements Runnable {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final RankingAiPlayer aiPlayer;
    private SelectDragonMoveEvent rootEv;

    public SelectDragonMoveTask(RankingAiPlayer aiPlayer, SelectDragonMoveEvent ev) {
        this.aiPlayer = aiPlayer;
        this.rootEv = ev;
    }

    @Override
    public void run() {
        try {
            selectDragonMove(aiPlayer.game);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            aiPlayer.selectDummyDragonMove(rootEv.getPositions(), rootEv.getMovesLeft());
        }
    }

    /**
     * simple heuristic
     */
    public void selectDragonMove(Game game) {
        Position dragonPosition = game.getCapability(DragonCapability.class).getDragon().getPosition();
        double tensionX = 0, tensionY = 0;

        for (Meeple m : game.getDeployedMeeples()) {
            int distance = dragonPosition.squareDistance(m.getPosition());
            if (distance == 0 || distance > rootEv.getMovesLeft()) continue;
            if (m.getFeature() instanceof Castle) continue;

            double weight = 1.0 / (distance * distance);
            if (aiPlayer.getPlayer().equals(m.getPlayer())) {
                weight *= -0.8;	//co takhle 0.8
            }
            tensionX += weight * (m.getPosition().x - dragonPosition.x);
            tensionY += weight * (m.getPosition().y - dragonPosition.y);
        }

        double minDiff = Double.MAX_VALUE;
        Position result = null;
        for (Position p : rootEv.getPositions()) {
            double diff =
                Math.abs(p.x - dragonPosition.x - tensionX) + Math.abs(p.y - dragonPosition.y - tensionY);
            if (diff < minDiff) {
                minDiff = diff;
                result = p;
            }
        }
        logger.info("Selected dragon move: {}", result);
        aiPlayer.getRmiProxy().moveDragon(result);
    }
}