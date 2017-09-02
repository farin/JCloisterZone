package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.LinkedHashMap;

public class DeployMeeple implements Reducer {

    private final Meeple meeple;
    private final FeaturePointer fp;

    public DeployMeeple(Meeple meeple, FeaturePointer fp) {
        this.meeple = meeple;
        this.fp = fp;
    }

    @Override
    public GameState apply(GameState state) {
        Structure feature = state.getStructure(fp);
        if (feature == null) {
            throw new IllegalArgumentException("There is no feature on " + fp);
        }

        DeploymentCheckResult check = meeple.isDeploymentAllowed(state, fp, feature);
        if (!check.result) {
            throw new IllegalArgumentException(check.error);
        }

        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.put(meeple, fp));
        state = state.appendEvent(
            new MeepleDeployed(PlayEventMeta.createWithActivePlayer(state), meeple, fp)
        );
        return state;
    }

}
