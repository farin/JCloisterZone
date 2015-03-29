package com.jcloisterzone.feature.visitor.score;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.primitives.Ints;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Game;

public class CloisterScoreContext extends AbstractScoreContext implements CompletableScoreContext {

    private int neigbouringTilesCount;
    protected Cloister cloister;


    public CloisterScoreContext(Game game) {
        super(game);
    }

    @Override
    public Cloister getMasterFeature() {
        return cloister;
    }

    @Override
    public int getPoints() {
        return neigbouringTilesCount + 1 + getLittleBuildingPoints();
    }

    @Override
    public Set<Position> getPositions() {
        return Collections.singleton(cloister.getTile().getPosition());
    }

    /**
     * Method filter out abbots. They are not used for common scoring.
     * @return
     */
    @Override
    public List<Follower> getFollowers() {
        List<Follower> follwers = new ArrayList<>();
        for (Meeple m : cloister.getMeeples()) {
            if (m.getLocation() != Location.ABBOT) {
                follwers.add((Follower) m);
            }
        }
        return follwers;
    }

    @Override
    public boolean visit(Feature feature) {
        cloister = (Cloister) feature;
        Position pos = cloister.getTile().getPosition();
        List<Tile> neigbouringTiles = game.getBoard().getAdjacentAndDiagonalTiles(pos);
        neigbouringTilesCount = neigbouringTiles.size();
        if (lbCap != null) {
        	collectLittleBuildings(cloister.getTile().getPosition());
        	for (Tile tile : neigbouringTiles) {
        		collectLittleBuildings(tile.getPosition());
        	}
        }
        return true;
    }

    @Override
    public Follower getSampleFollower(Player player) {
        for (Follower m : getFollowers()) {
            if (m.getPlayer() == player) return m;
        }
        return null;
    }

    @Override
    public Set<Player> getMajorOwners() {
        Collection<Follower> followers = getFollowers();
        int size = followers.size();
        if (size == 0) return Collections.emptySet();
        if (size == 1) return Collections.singleton(followers.iterator().next().getPlayer());

        //rare case - more then one follower placed on cloister (possible by Flier expansion)
        int[] power = new int[game.getAllPlayers().length];
        for (Follower f : followers) {
            power[f.getPlayer().getIndex()] += f.getPower();
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

    @Override
	public Map<Player, Integer> getPowers() {
        Collection<Follower> followers = getFollowers();
        int size = followers.size();
        if (size == 0) return Collections.emptyMap();
        if (size == 1) {
            Follower follower = followers.iterator().next();
            return Collections.singletonMap(follower.getPlayer(), follower.getPower());
        }
        //rare cases
        Map<Player, Integer> result = new HashMap<Player, Integer>();
        for (Follower follower : followers) {
            Integer val = result.get(follower.getPlayer());
            result.put(follower.getPlayer(), val == null ? follower.getPower() : val + follower.getPower());
        }
        return result;

    }

    @Override
    public List<Special> getSpecialMeeples() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<? extends Meeple> getMeeples() {
        return getFollowers();
    }

    @Override
    public boolean isCompleted() {
        return neigbouringTilesCount == 8;
    }



}
