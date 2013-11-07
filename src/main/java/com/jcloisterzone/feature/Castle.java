package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.visitor.score.ScoreContext;

public class Castle extends MultiTileFeature {

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CASTLE;
    }

    @Override
    public ScoreContext getScoreContext() {
        throw new UnsupportedOperationException();
    }

    public Castle getSecondFeature() {
        return (Castle) getEdges()[0];
    }

    @Override
    public Castle getMaster() {
        Castle other = getSecondFeature();
        return getId() < other.getId() ? this : other;
    }

    public Position[] getCastleBase() {
        Position[] positions = new Position[6];
        positions[0] = getTile().getPosition();
        positions[1] = getSecondFeature().getTile().getPosition();
        return positions;
    }

    public Position[] getVicinity() {
        Position[] vicinity = new Position[6];
        vicinity[0] = getTile().getPosition();
        vicinity[1] = getSecondFeature().getTile().getPosition();
        if (vicinity[0].x == vicinity[1].x) {
            vicinity[2] = vicinity[0].add(Location.W);
            vicinity[3] = vicinity[0].add(Location.E);
            vicinity[4] = vicinity[1].add(Location.W);
            vicinity[5] = vicinity[1].add(Location.E);
        } else {
            vicinity[2] = vicinity[0].add(Location.N);
            vicinity[3] = vicinity[0].add(Location.S);
            vicinity[4] = vicinity[1].add(Location.N);
            vicinity[5] = vicinity[1].add(Location.S);
        }
        return vicinity;
    }

    public static String name() {
        return _("Castle");
    }

}
