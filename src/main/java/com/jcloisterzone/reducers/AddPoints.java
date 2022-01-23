package com.jcloisterzone.reducers;

import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Array;
import io.vavr.collection.List;

public class AddPoints implements Reducer {

    private final ScoreEvent scoreEvent;

    public AddPoints(List<ScoreEvent.ReceivedPoints> points, boolean landscapeSource, boolean isFinal) {
        this.scoreEvent = new ScoreEvent(points, landscapeSource, isFinal);
    }

    public AddPoints(ScoreEvent.ReceivedPoints points, boolean landscapeSource, boolean isFinal) {
        this(List.of(points), landscapeSource, isFinal);
    }

    public AddPoints(List<ScoreEvent.ReceivedPoints> points, boolean landscapeSource) {
        this(points, landscapeSource, false);
    }

    public AddPoints(ScoreEvent.ReceivedPoints points, boolean landscapeSource) {
        this(List.of(points), landscapeSource, false);
    }

    @Override
    public GameState apply(GameState state) {
        state = state.appendEvent(scoreEvent);
        for (ScoreEvent.ReceivedPoints pts : scoreEvent.getPoints()) {
            if (pts.getPoints() == 0) continue;

            int idx = pts.getPlayer().getIndex();
            state = state.mapPlayers(ps -> {
                Array<Integer> score = ps.getScore();
                score = score.update(idx, score.get(idx) + pts.getPoints());
                return ps.setScore(score);
            });
        }
        return state;
    }
}


