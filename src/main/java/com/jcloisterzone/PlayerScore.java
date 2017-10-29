package com.jcloisterzone;

import java.io.Serializable;

import io.vavr.collection.HashMap;
import io.vavr.control.Option;

@Immutable
public class PlayerScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int points;
    private final HashMap<PointCategory, Integer> stats;

    public PlayerScore() {
        this.points = 0;
        this.stats = HashMap.empty();
    }

    public PlayerScore(int points, HashMap<PointCategory, Integer> stats) {
        this.points = points;
        this.stats = stats;
    }

    public PlayerScore addPoints(int points, PointCategory category) {
        Option<Integer> value = stats.get(category);
        HashMap<PointCategory, Integer> stats;
        if (value.isEmpty()) {
            stats = this.stats.put(category, points);
        } else {
            stats = this.stats.put(category, value.get() + points);
        }
        return new PlayerScore(this.points + points, stats);
    }

    public int getPoints() {
        return points;
    }

    public HashMap<PointCategory, Integer> getStats() {
        return stats;
    }
}
