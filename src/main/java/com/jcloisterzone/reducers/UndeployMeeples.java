package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeepleReturned;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Shepherd;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.ArrayList;


/**
 * Undeploys all meeples except Barn ans Shepherds! Barns are never returned and shepherds has special handling;
 */
public class UndeployMeeples implements Reducer {

    private final Feature feature;
    /** true if meeple is returned different way than scoring feature */
    private final boolean forced;

    public UndeployMeeples(Feature feature, boolean forced) {
        this.feature = feature;
        this.forced = forced;

    }

    @Override
    public GameState apply(GameState state) {
        Set<FeaturePointer> fps = HashSet.ofAll(feature.getPlaces());
        ArrayList<Meeple> meeples = new ArrayList<>();
        ArrayList<PlayEvent> events = new ArrayList<>();
        PlayEventMeta eventMeta = PlayEventMeta.createWithoutPlayer();

        for (Tuple2<Meeple, FeaturePointer> t : state
                .getDeployedMeeples()
                .filter(t -> fps.contains(t._2))
                .filter(t -> !(t._1 instanceof Barn) &&  !(t._1 instanceof Shepherd))
            ) {
            meeples.add(t._1);
            events.add(
                new MeepleReturned(eventMeta, t._1, t._2, forced)
            );
        }
        state = state.setDeployedMeeples(
            state.getDeployedMeeples().removeAll(meeples)
        );
        state = state.setEvents(
            state.getEvents().appendAll(events)
        );

        NeutralFiguresState nfState = state.getNeutralFigures();
        BoardPointer fairyPtr =  nfState.getFairyDeployment();
        if (fairyPtr instanceof MeeplePointer) {
            MeeplePointer mp = (MeeplePointer) fairyPtr;
            for (Meeple meeple : meeples) {
                if (meeple.getId().equals(mp.getMeepleId())) {
                    mp = new MeeplePointer(mp.asFeaturePointer(), null);
                    nfState = nfState.setDeployedNeutralFigures(nfState.getDeployedNeutralFigures().put(nfState.getFairy(), mp));
                    state = state.setNeutralFigures(nfState);
                    break;
                }
            }
        }

        return state;
    }

    public boolean isForced() {
        return forced;
    }

}
