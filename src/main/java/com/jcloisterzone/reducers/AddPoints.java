package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Array;

public class AddPoints implements Reducer {

    final Player player;
    final int points;

    public AddPoints(Player player, int points) {
        this.player = player;
        this.points = points;
    }

    @Override
    public GameState apply(GameState state) {
        if (points == 0) {
            return state;
        }

        int idx = player.getIndex();
        return state.mapPlayers(ps -> {
            Array<Integer> score = ps.getScore();
            score = score.update(idx, score.get(idx) + points);
            return ps.setScore(score);
        });
    }

}
