package com.jcloisterzone.reducers;

import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.PrisonersExchangeEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.GameState;

public class PrisonersExchage implements Reducer {

    private final Follower a;
    private final Follower b;


    public PrisonersExchage(Follower a, Follower b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public GameState apply(GameState state) {
        state = state.mapCapabilityModel(TowerCapability.class, model -> {
            model = model.update(b.getPlayer().getIndex(), l -> l.remove(a));
            model = model.update(a.getPlayer().getIndex(), l -> l.remove(b));
            return model;
        });
        state = state.appendEvent(new PrisonersExchangeEvent(
            PlayEventMeta.createWithoutPlayer(), a, b)
        );
        return state;
    }

}
