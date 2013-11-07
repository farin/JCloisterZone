package com.jcloisterzone.ai.legacyplayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.ai.AiScoreContext;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.SelfReturningVisitor;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;

class LegacyAiScoreContext extends SelfReturningVisitor implements CompletableScoreContext, AiScoreContext {

    public static class OpenEdge {
        double chanceToClose;
        Feature feature;
        Location location;
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final LegacyAiPlayer aiPlayer;
    private final CompletableScoreContext ctx;
    //private final Map<Feature, AiScoreContext> scoreCache;
    private boolean valid = true;

    private Map<Position, OpenEdge> openEdgesChanceToClose = new HashMap<>();
    private double chanceToClose = 1.0;

    public LegacyAiScoreContext(LegacyAiPlayer aiPlayer, CompletableScoreContext ctx/*, Map<Feature, AiScoreContext> scoreCache*/) {
        this.aiPlayer = aiPlayer;
        this.ctx = ctx;
        //this.scoreCache = scoreCache;
    }

    @Override
    public boolean isValid() {
        return valid;
    };

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public double getChanceToClose() {
        if (chanceToClose > 0.95) {
            return 0.95;
        }
        return chanceToClose;
    }

    private double updateCompletableChanceToClose(CompletableFeature completable) {
        double result = 1.0;
        //TODO: this method using internal contract of MultiTileFeature - encapsulation is violated here
        int i = 0;
        for (Location side : Location.sides()) {
            if (side.intersect(completable.getLocation()) != null) {
                if (completable.getEdges()[i] == null) {
                    //side is open
                    Position p = completable.getTile().getPosition().add(side);
                    if (!openEdgesChanceToClose.containsKey(p)) {
                        OpenEdge edge = new OpenEdge();
                        edge.chanceToClose = aiPlayer.chanceToPlaceTile(p);
                        edge.feature = completable;
                        edge.location = side;
                        openEdgesChanceToClose.put(p, edge);
                        result *= edge.chanceToClose;
                    }
                }
                i++;
            }
        }
        return result;
    }

    public Map<Position, OpenEdge> getOpenEdgesChanceToClose() {
        return openEdgesChanceToClose;
    }

    private double updateCloisterChanceToClose(Cloister cloister) {
        double result = 1.0;
        Position p = cloister.getTile().getPosition();
        for (Position adjacent: p.addMulti(Position.ADJACENT_AND_DIAGONAL.values())) {
            result *= aiPlayer.chanceToPlaceTile(adjacent);
        }
        //for "1.6-compatibility" - make it already sense ?
        if (result > 0.85) return 0.85;
        return result;
    }

    @Override
    public boolean visit(Feature feature) {
        //scoreCache.put(feature, this);

        if (feature instanceof CompletableFeature) {
            chanceToClose *= updateCompletableChanceToClose((CompletableFeature) feature);
        } else if (feature instanceof Cloister) {
            chanceToClose *= updateCloisterChanceToClose((Cloister) feature);
        }
        return ctx.visit(feature);
    }

    public CompletableScoreContext getCompletableScoreContext() {
        return ctx;
    }

    @Override
    public Follower getSampleFollower(Player player) {
        return ctx.getSampleFollower(player);
    }

    @Override
    public Set<Player> getMajorOwners() {
        return ctx.getMajorOwners();
    }

    @Override
    public List<Follower> getFollowers() {
        return ctx.getFollowers();
    }

    @Override
    public List<Special> getSpecialMeeples() {
        return ctx.getSpecialMeeples();
    }

    @Override
    public Iterable<Meeple> getMeeples() {
        return ctx.getMeeples();
    }


    @Override
    public Completable getMasterFeature() {
        return ctx.getMasterFeature();
    }

    @Override
    public boolean isCompleted() {
        return ctx.isCompleted();
    }

    @Override
    public int getPoints() {
        return ctx.getPoints();
    }

    @Override
    public Set<Position> getPositions() {
        return ctx.getPositions();
    }

    @Override
    public Map<Player, Integer> getPowers() {
        return ctx.getPowers();
    }

}