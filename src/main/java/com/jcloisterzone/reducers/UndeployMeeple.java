package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeepleReturned;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public class UndeployMeeple extends AbstractUndeploy {

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

        if (meeple instanceof Follower) {
            // Undeploy lonely Builders and Pigs
            state = undeployLonelySpecials(state, (Follower) meeple, source, forced);
        }

        if (meeple instanceof Shepherd) {
        	state = state.mapCapabilityModel(SheepCapability.class, tokens -> tokens.remove(source));
        }

        NeutralFiguresState nfState = state.getNeutralFigures();
        BoardPointer fairyPtr =  nfState.getFairyDeployment();
        if (fairyPtr instanceof MeeplePointer) {
            MeeplePointer mp = (MeeplePointer) fairyPtr;
            if (meeple.getId().equals(mp.getMeepleId())) {
                mp = new MeeplePointer(mp.asFeaturePointer(), null);
                nfState = nfState.setDeployedNeutralFigures(nfState.getDeployedNeutralFigures().put(nfState.getFairy(), mp));
                state = state.setNeutralFigures(nfState);
            }
        }
        return state;
    }

    protected GameState primaryUndeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        return undeploy(state, meta, meeple, source, forced);
    }

    public boolean isForced() {
        return forced;
    }
}
