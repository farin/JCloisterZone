package com.jcloisterzone.game;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import io.vavr.collection.List;

@Immutable
public class BonusPoints {

    private final int points;
    private final PointCategory pointCategory;
    private final Player player;
    private final Follower follower;
    private final List<Position> source; // hack


    public BonusPoints(int points, PointCategory pointCategory, Player player, Follower follower, List<Position> source) {
        this.points = points;
        this.pointCategory = pointCategory;
        this.player = player;
        this.follower = follower;
        this.source = source;
    }

    public int getPoints() {
        return points;
    }

    public PointCategory getPointCategory() {
        return pointCategory;
    }

    public Player getPlayer() {
        return player;
    }

    public Follower getFollower() {
        return follower;
    }

    public List<Position> getSource() {
        return source;
    }
}
