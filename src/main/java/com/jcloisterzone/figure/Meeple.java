package com.jcloisterzone.figure;

import com.google.common.base.Objects;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Meeple extends Figure {

    private static final long serialVersionUID = 251811435063355665L;

    private transient final Player player;
    private transient Feature feature;
    private transient Integer index; //index distinguish meeples on same feature


    public static class DeploymentCheckResult {
        public final boolean result;
        public final String error;

        private DeploymentCheckResult() {
            this.result = true;
            this.error = null;
        }

        public DeploymentCheckResult(String error) {
            this.result = false;
            this.error = error;
        }

        public static final DeploymentCheckResult OK = new DeploymentCheckResult();
    }

    public Meeple(Game game, Player player) {
        super(game);
        this.player = player;
    }

    public boolean canBeEatenByDragon() {
        return true;
    }

    public void clearDeployment() {
        setFeaturePointer(null);
        setFeature(null);
    }

    public DeploymentCheckResult isDeploymentAllowed(Feature feature) {
        return DeploymentCheckResult.OK;
    }

    public Feature getDeploymentFeature(Tile tile, Location loc) {
        return tile.getFeature(loc);
    }


    @Override
	public void deploy(FeaturePointer at) {
    	Feature feature = game.getBoard().get(at);
        DeploymentCheckResult check = isDeploymentAllowed(feature);
        if (!check.result) {
            throw new IllegalArgumentException(check.error);
        }
        FeaturePointer origin = getFeaturePointer();
        feature.addMeeple(this);
        setFeaturePointer(at);
        setFeature(feature);
        game.post(new MeepleEvent(game.getActivePlayer(), this, origin, at));
    }

    @Override
	public final void undeploy() {
        undeploy(true);
    }

    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        assert isDeployed();
        FeaturePointer source = getFeaturePointer();
        feature.removeMeeple(this);
        clearDeployment();
        game.post(new MeepleEvent(game.getActivePlayer(), this, source, null));
    }


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature piece) {
        this.feature = piece;
    }

    public Player getPlayer() {
        return player;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(index, featurePointer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false; //compares exact types
        Meeple o = (Meeple) obj;
        if (!Objects.equal(player, o.player)) return false;
        if (!Objects.equal(index, o.index)) return false;
        //do not compare feature - location is enough - feature is changing during time
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + player.getIndex() + ")";
    }

}
