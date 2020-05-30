package com.jcloisterzone.reducers;

import java.util.function.Predicate;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.RansomPaidEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Array;
import io.vavr.collection.List;

public class PayRansom implements Reducer {

    private final String meepleId;

    public PayRansom(String meepleId) {
        this.meepleId = meepleId;
    }

    public GameState apply(GameState state) {
        if (state.hasFlag(Flag.RANSOM_PAID)) {
            throw new IllegalStateException("Ransom can be paid only once a turn.");
        }

        Player player = state.getActivePlayer();
        Predicate<Follower> pred = f -> f.getId().equals(meepleId);

        Array<List<Follower>> model = state.getCapabilityModel(TowerCapability.class);
        Follower follower = null;
        Player jailer = null;

        for (int i = 0; i < model.length(); i++) {
            follower = model.get(i).find(pred).getOrNull();
            if (follower != null) {
                jailer = state.getPlayers().getPlayer(i);
                break;
            }
        }

        if (follower == null) {
            throw new IllegalArgumentException(String.format("No such prisoner %s.", meepleId));
        }
        if (!follower.getPlayer().equals(player)) {
            new IllegalArgumentException("Cannot pay ransom for opponent's follower.");
        }

        Player _jailer = jailer;
        Follower _follower = follower;
        state = state.mapCapabilityModel(TowerCapability.class, m ->
            m.update(_jailer.getIndex(), l -> l.remove(_follower))
        );
        state = (new AddPoints(player, -TowerCapability.RANSOM_POINTS, PointCategory.TOWER_RANSOM)).apply(state);
        state = (new AddPoints(jailer, TowerCapability.RANSOM_POINTS, PointCategory.TOWER_RANSOM)).apply(state);
        state = state.addFlag(Flag.RANSOM_PAID);
        state = state.appendEvent(
                new RansomPaidEvent(PlayEvent.PlayEventMeta.createWithActivePlayer(state), follower, jailer)
        );
        //TODO add PlayEvent

        return state;
    }

}

