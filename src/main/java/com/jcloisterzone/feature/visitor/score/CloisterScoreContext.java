package com.jcloisterzone.feature.visitor.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.primitives.Ints;
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

    @Override
    public Follower getSampleFollower(Player player) {
        for (Meeple m : cloister.getMeeples()) {
            if (m instanceof Follower && m.getPlayer() == player) return (Follower) m;
        }
        return null;
    }

    @Override
    public Set<Player> getMajorOwners() {
        int size = cloister.getMeeples().size();
        if (size == 0) return Collections.emptySet();
        if (size == 1) return Collections.singleton(cloister.getMeeples().iterator().next().getPlayer());

        //rare case - more then one follower placed on cloister (possible by Flier expansion)
        int[] power = new int[game.getAllPlayers().length];
        for (Meeple m : cloister.getMeeples()) {
            if (m instanceof Follower) {
                Follower f = (Follower) m;
                power[f.getPlayer().getIndex()] += f.getPower();
            }
        }
        int maxPower = Ints.max(power);
        Set<Player> owners = new HashSet<>();
        for (int i = 0; i < power.length; i++) {
            if (power[i] == maxPower) {
                owners.add(game.getAllPlayers()[i]);
            }
        }
        return owners;
    }

    public Map<Player, Integer> getPowers() {
        int size = cloister.getMeeples().size();
        if (size == 0) return Collections.emptyMap();
        if (size == 1) {
            Follower m = (Follower) cloister.getMeeples().iterator().next();
            return Collections.singletonMap(m.getPlayer(), m.getPower());
        }
        //rare cases
        Map<Player, Integer> result = new HashMap<Player, Integer>();
        for (Meeple m : cloister.getMeeples()) {
            Follower follower = (Follower) m;
            Integer val = result.get(follower.getPlayer());
            result.put(follower.getPlayer(), val == null ? follower.getPower() : val + follower.getPower());
        }
        return result;

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Follower> getFollowers() {
        //nowdays only followers can be placed on cloister
        List<Follower> followers = (List<Follower>)(Object)cloister.getMeeples();
        //copy required - origin is modified during copy iteration
        return new ArrayList<Follower>(followers);
    }

    @Override
    public List<Special> getSpecialMeeples() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<Meeple> getMeeples() {
        //copy required - origin is modified during copy iteration
        return new ArrayList<Meeple>(cloister.getMeeples());
    }

    @Override
    public boolean isCompleted() {
        return neigbouringTiles == 8;
    }

}
