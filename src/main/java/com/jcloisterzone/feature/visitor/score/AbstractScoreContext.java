package com.jcloisterzone.feature.visitor.score;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.visitor.SelfReturningVisitor;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Game;

public abstract class AbstractScoreContext extends SelfReturningVisitor implements ScoreContext {

	private final Game game;

	private Scoreable feature;

	int bestPower = 1; //minimal power to score must be 1 (mayor power can be 0)

	private Map<Player, Integer> power = Maps.newHashMap();
	private Map<Player, Follower> sample = Maps.newHashMap();
	private Position preferedPos = null;

	private List<Follower> followers = Lists.newArrayList();
	private List<Special> specialMeeples = Lists.newArrayList();

	public AbstractScoreContext(Game game) {
		this.game = game;
		if (game.hasExpansion(Expansion.PRINCESS_AND_DRAGON)) {
			preferedPos = game.getPrincessAndDragonGame().getFairyPosition();
		}
	}

	public Game getGame() {
		return game;
	}

	@Override
	public Scoreable getMasterFeature() {
		return feature;
	}

	@Override
	public boolean visit(Feature feature) {
		Meeple meeple = feature.getMeeple();
		if (meeple instanceof Follower) {
			Follower follower = (Follower) meeple;
			Integer prev = power.get(follower.getPlayer());
			int followerPower = follower.getPower();
			int curr = prev == null ? followerPower : prev + followerPower;
			power.put(follower.getPlayer(), curr);
			if (curr > bestPower) bestPower = curr;
			if (sample.containsKey(follower.getPlayer())) {
				if (follower.getPosition().equals(preferedPos)) {
					sample.put(follower.getPlayer(), follower);
				}
			} else {
				sample.put(follower.getPlayer(), follower);
			}
		}
		if (meeple instanceof Follower) {
			followers.add((Follower) meeple);
		} else {
			if (meeple instanceof Special) {
				specialMeeples.add((Special) meeple);
			}
		}


		if (this.feature == null || this.feature.getId() > feature.getId()) {
			this.feature = (Scoreable) feature;
		}
		return true;
	}

	public Follower getSampleFollower(Player player) {
		return sample.get(player);
	}

	public Set<Player> getMajorOwners() {
		Set<Player> majorOwners = Sets.newHashSet();
		for(Player player: power.keySet()) {
			int pwr = power.get(player);
			if (pwr == bestPower) {
				majorOwners.add(player);
			}
		}
		return majorOwners;
	}

	public List<Follower> getFollowers() {
		return followers;
	}

	public List<Special> getSpecialMeeples() {
		return specialMeeples;
	}

	@Override
	public Iterable<Meeple> getMeeples() {
		return Iterables.<Meeple>concat(followers, specialMeeples);
	}

}

