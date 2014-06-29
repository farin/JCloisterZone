package com.jcloisterzone.feature.visitor.score;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class MonasteryAbbotScoreContext extends CloisterScoreContext {

    public MonasteryAbbotScoreContext(Game game) {
        super(game);
    }

    @Override
    public List<Follower> getFollowers() {
        List<Follower> follwers = new ArrayList<>();
        for (Meeple m : cloister.getMeeples()) {
            if (m.getLocation() == Location.ABBOT) {
                follwers.add((Follower) m);
            }
        }
        return follwers;
    }

    @Override
    public int getPoints() {
        int points = 1;
        Position monasteryPosition = cloister.getTile().getPosition();
        for (Location loc : Location.sides()) {
            points += game.getBoard().getContinuousRowSize(monasteryPosition, loc);
        }
        return points;
    }

    @Override
    public boolean isCompleted() {
        //TOOD what about extend monastery context from AbstractScoreContext
        throw new UnsupportedOperationException("No sense for manastery with abbot");
    }


}
