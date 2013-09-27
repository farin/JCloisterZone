package com.jcloisterzone.figure;

import com.google.common.base.Objects;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Meeple extends Figure {

    private static final long serialVersionUID = 251811435063355665L;

    private transient final Player player;
    private transient Feature feature;
    private transient Integer index; //index distinguish meeples on same feature
    private Location location;

    public Meeple(Game game, Player player) {
        super(game);
        this.player = player;
    }

    public boolean canBeEatenByDragon() {
        return true;
    }

    public boolean isDeployed() {
        //must check only location  because prisoner has everything except location also null
        return location != null;
    }

    public void clearDeployment() {
        setPosition(null);
        setLocation(null);
        setFeature(null);
    }

    protected void checkDeployment(Feature piece) {
        //empty
    }

    public Feature getPieceForDeploy(Tile tile, Location loc) {
        Feature piece = tile.getFeature(loc);
        if (piece == null) {
            throw new IllegalArgumentException("No such feature");
        }
        return piece;
    }

    public void deploy(Tile tile, Location loc) {
        Feature feature = getPieceForDeploy(tile, loc);
        checkDeployment(feature);
        deployUnchecked(tile, loc, feature);
        game.fireGameEvent().deployed(this);
    }

    public void deployUnchecked(Tile tile, Location loc, Feature feature) {
        feature.addMeeple(this);
        setPosition(tile.getPosition());
        setLocation(loc);
        setFeature(feature);
    }

    public final void undeploy() {
        undeploy(true);
    }

    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        game.fireGameEvent().undeployed(this);
        feature.removeMeeple(this);
        clearDeployment();
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        return 47 * getPlayer().getIndex() + 13 * (index == null ? 0 : (int) index) + (location == null ? 1 : location.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Meeple)) return false;
        if (!super.equals(obj)) return false;
        Meeple o = (Meeple) obj;
        if (!Objects.equal(index, o.index)) return false;
        if (!Objects.equal(location, o.location)) return false;
        //do not compare feature - location is enough - feature is changing during time
        return true;
    }

}
