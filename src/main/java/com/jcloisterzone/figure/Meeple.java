package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;

@Immutable
public abstract class Meeple extends Figure<FeaturePointer> {

    private static final long serialVersionUID = 251811435063355665L;

    private transient final Player player;

    public Meeple(String id, Player player) {
        super(id);
        this.player = player;
    }

    @Override
    public FeaturePointer getDeployment(GameState state) {
        return state.getDeployedMeeples().get(this).getOrNull();
    }

    @Override
    public boolean at(GameState state, Structure feature) {
        return feature.getMeeples(state).find(Predicates.is(this)).isDefined();
    }

    public boolean at(GameState state, MeeplePointer mp) {
        if (!at(state, mp.asFeaturePointer())) return false;
        if (mp.getMeepleId() == null || !mp.getMeepleId().equals(getId())) return false;
        return true;
    }

    public boolean canBeEatenByDragon(GameState state) {
        return true;
    }

    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Structure feature) {
        return DeploymentCheckResult.OK;
    }

    public Player getPlayer() {
        return player;
    }
}
