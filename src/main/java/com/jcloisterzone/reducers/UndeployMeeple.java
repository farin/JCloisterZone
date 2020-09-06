package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleReturned;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public class UndeployMeeple implements Reducer {

    private final Meeple meeple;
    /** true if meeple is returned different way than scoring feature */
    private final boolean forced;

    public UndeployMeeple(Meeple meeple, boolean forced) {
        this.meeple = meeple;
        this.forced = forced;
    }

    @Override
    public GameState apply(GameState state) {
        FeaturePointer source = meeple.getDeployment(state);
        assert source != null;

        PlayEventMeta metaWithPlayer = PlayEventMeta.createWithActivePlayer(state);
        state = primaryUndeploy(state, metaWithPlayer, meeple, source);

        // Undeploy lonely Builders and Pigs
        if (meeple instanceof Follower) {
            Player owner = meeple.getPlayer();
            PlayEventMeta metaNoPlayer = PlayEventMeta.createWithoutPlayer();
            Structure feature = state.getStructure(source);
            Stream<Tuple2<Meeple, FeaturePointer>> threatened = feature.getMeeples2(state)
                .filter(m -> (m._1 instanceof Pig) || (m._1 instanceof Builder))
                .filter(m -> m._1.getPlayer().equals(owner));

            for (Tuple2<Meeple, FeaturePointer> t : threatened) {
                if (feature.getFollowers(state).find(f -> f.getPlayer().equals(owner)).isEmpty()) {
                    state = undeploy(state, metaNoPlayer, t._1, t._2);
                }
            }
        }

        if (meeple instanceof Shepherd) {
        	state = state.mapCapabilityModel(SheepCapability.class, tokens -> tokens.remove(source));
        }

        return state;
    }

    protected GameState primaryUndeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        return undeploy(state, meta, meeple, source);
    }

    private GameState undeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.remove(meeple));
        state = state.appendEvent(
            new MeepleReturned(meta, meeple, source, forced)
        );
        return state;
    }

    public boolean isForced() {
        return forced;
    }

}
