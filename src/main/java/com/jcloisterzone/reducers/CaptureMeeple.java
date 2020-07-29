package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.FollowerCaptured;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.LinkedHashMap;

public class CaptureMeeple extends UndeployMeeple {

    public CaptureMeeple(Follower follower) {
        super(follower, true);
    }

    @Override
    protected GameState primaryUndeploy(GameState state, PlayEventMeta meta, Meeple meeple, FeaturePointer source) {
        Follower follower = (Follower) meeple;
        Player p = state.getPlayers().getPlayer(meta.getTriggeringPlayerIndex());
        if (p.equals(follower.getPlayer())) {
            return super.primaryUndeploy(state, meta, follower, source);
        } else {
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples = state.getDeployedMeeples();
            state = state.setDeployedMeeples(deployedMeeples.remove(follower));
            state = state.mapCapabilityModel(TowerCapability.class, model -> {
                return model.update(p.getIndex(), l -> l.append(follower));
            });
            state = state.appendEvent(
                new FollowerCaptured(meta, follower, source)
            );
            return state;
        }
    }
}
