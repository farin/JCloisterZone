package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

@Immutable
public abstract class Follower extends Meeple {

    private static final long serialVersionUID = 1L;

    public Follower(String id, Player player) {
        super(id, player);
    }

    public int getPower(GameState state, Scoreable feature) {
        return 1;
    }

    @Override
    public boolean canBeEatenByDragon(GameState state) {
        return !(getFeature(state) instanceof Castle);
    }

    public boolean isCaptured(GameState state) {
        Array<List<Follower>> model = state.getCapabilityModel(TowerCapability.class);
        return model != null && Stream.concat(model).find(f -> f == this).isDefined();
    }

    @Override
    public boolean isInSupply(GameState state) {
        return super.isInSupply(state) && !isCaptured(state);
    }
}
