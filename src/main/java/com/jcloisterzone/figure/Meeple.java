package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Meeple extends Figure {

    private static final long serialVersionUID = 251811435063355665L;

    private final String id;

    private transient final Player player;
    private transient Feature feature;
    private transient Integer index; //order of deployment on same feature //TODO rename variable


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

    public Meeple(Game game, Integer idSuffix, Player player) {
        super(game);
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(player.getIndex());
        idBuilder.append(".");
        idBuilder.append(getClass().getSimpleName());
        if (idSuffix != null) {
            idBuilder.append(".");
            idBuilder.append(idSuffix.toString());
        }
        this.id = idBuilder.toString();
        this.player = player;
    }

    public String getId() {
        return id;
    }

    public boolean at(MeeplePointer mp) {
        if (!at(mp.asFeaturePointer())) return false;
        if (!mp.getMeepleId().equals(id)) return false;
        return true;
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
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Meeple)) return false;
        return this == obj || id.equals(((Meeple)obj).id);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + player.getNick() + "," + id + ")";
    }
}
