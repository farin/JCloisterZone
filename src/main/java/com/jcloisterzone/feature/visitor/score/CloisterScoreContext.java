package com.jcloisterzone.feature.visitor.score;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.SelfReturningVisitor;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Game;

public class CloisterScoreContext extends SelfReturningVisitor implements CompletableScoreContext {

    private int neigbouringTiles;
    private Cloister cloister;
    private Game game;

    public CloisterScoreContext(Game game) {
        this.game = game;
    }

    @Override
    public Cloister getMasterFeature() {
        return cloister;
    }

    @Override
    public int getPoints() {
        return neigbouringTiles + 1;
    }

    @Override
    public Set<Position> getPositions() {
        return Collections.singleton(cloister.getTile().getPosition());
    }

    @Override
    public boolean visit(Feature feature) {
        cloister = (Cloister) feature;
        Position pos = cloister.getTile().getPosition();
        neigbouringTiles = game.getBoard().getAdjacentAndDiagonalTiles(pos).size();
        return true;
    }

    /**
     * Currently there can be only one follower on cloister feature ( in comparison with other features like City, ...)
     * So nowdays this helper method can be used
     */
    private Meeple getCloisterMeeple() {
        List<Meeple> meeples = cloister.getMeeples();
        if (meeples.isEmpty()) return null;
        return meeples.get(0);
    }

    @Override
    public Follower getSampleFollower(Player player) {
        if (getCloisterMeeple().getPlayer() == player) return (Follower) getCloisterMeeple();
        return null;
    }

    @Override
    public Set<Player> getMajorOwners() {
        if (getCloisterMeeple() == null) return Collections.emptySet();
        return Collections.singleton(getCloisterMeeple().getPlayer());
    }

    @Override
    public List<Follower> getFollowers() {
        if (getCloisterMeeple() == null) return Collections.emptyList();
        return Collections.singletonList((Follower) getCloisterMeeple());
    }

    @Override
    public List<Special> getSpecialMeeples() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<Meeple> getMeeples() {
        return Iterables.<Meeple>concat(getFollowers(), getSpecialMeeples());
    }

    @Override
    public boolean isCompleted() {
        return neigbouringTiles == 8;
    }

}
