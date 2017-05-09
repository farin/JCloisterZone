package com.jcloisterzone.reducers;

import java.util.ArrayList;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/**
 * Undeploys all meeples except Barn! Barns are never returned.
 */
public class UndeployMeeples implements Reducer {

    private final Feature feature;

    public UndeployMeeples(Feature feature) {
        this.feature = feature;
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
                .filter(t -> !(t._1 instanceof Barn))
            ) {
            meeples.add(t._1);
            events.add(
                new MeepleReturned(eventMeta, t._1, t._2)
            );
        }
        state = state.setDeployedMeeples(
            state.getDeployedMeeples().removeAll(meeples)
        );
        state = state.setEvents(
            state.getEvents().appendAll(events)
        );

                // IMMUTABLE TOOD Mage and Witch undeploy
//            if (ctx instanceof PositionCollectingScoreContext) {
//                PositionCollectingScoreContext pctx = (PositionCollectingScoreContext) ctx;
//                if (pctx.containsMage()) {
//                    mageWitchCap.getMage().undeploy();
//                }
//                if (pctx.containsWitch()) {
//                    mageWitchCap.getWitch().undeploy();
//                }
//            }
//        }

        return state;
    }

}
