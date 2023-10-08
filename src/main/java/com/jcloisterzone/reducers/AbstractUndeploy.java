package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleReturned;
import com.jcloisterzone.event.NeutralFigureReturned;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public abstract class AbstractUndeploy implements Reducer {

    protected GameState undeploy(GameState state, PlayEvent.PlayEventMeta meta, Meeple meeple, FeaturePointer source, boolean forced) {
        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.remove(meeple));
        state = state.appendEvent(
            new MeepleReturned(meta, meeple, source, forced)
        );
        return state;
    }

    protected GameState undeployLonelySpecials(GameState state, Follower meeple, FeaturePointer source, boolean forced) {
        Player owner = meeple.getPlayer();
        PlayEvent.PlayEventMeta metaNoPlayer = PlayEvent.PlayEventMeta.createWithoutPlayer();
        Structure feature = state.getStructure(source);
        Stream<Tuple2<Meeple, FeaturePointer>> threatened = feature.getMeeples2(state)
                .filter(m -> (m._1 instanceof Pig) || (m._1 instanceof Builder))
                .filter(m -> m._1.getPlayer().equals(owner));

        for (Tuple2<Meeple, FeaturePointer> t : threatened) {
            if (feature.getFollowers(state).find(f -> f.getPlayer().equals(owner)).isEmpty()) {
                state = undeploy(state, metaNoPlayer, t._1, t._2, forced);
            }
        }

        return state;
    }

    protected GameState undeploy(GameState state, PlayEvent.PlayEventMeta meta, NeutralFigure<?> figure, BoardPointer source, boolean forced, Player player) {
        
        NeutralFiguresState nfState = state.getNeutralFigures();
        nfState = nfState.setDeployedNeutralFigures(nfState.getDeployedNeutralFigures().remove(figure));
        state = state.setNeutralFigures(nfState);

        state = state.appendEvent(
            new NeutralFigureReturned(meta, figure, source, forced, player)
        );
        return state;
    }
}
