package com.jcloisterzone.event;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import io.vavr.collection.List;

@Immutable
public class ScoreEvent extends PlayEvent  {

    private static final long serialVersionUID = 1L;

    private final List<ReceivedPoints> points;
    private final boolean landscapeSource;
    private final boolean isFinal;

    public ScoreEvent(List<ReceivedPoints> points, boolean landscapeSource, boolean isFinal) {
        super(PlayEventMeta.createWithoutPlayer());
        this.points = points;
        this.landscapeSource = landscapeSource;
        this.isFinal = isFinal;
    }

    public ScoreEvent(ReceivedPoints points, boolean landscapeSource, boolean isFinal) {
        this(List.of(points), landscapeSource, isFinal);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<ReceivedPoints> getPoints() {
        return points;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isLandscapeSource() {
        return landscapeSource;
    }

    @Immutable
    public static class ReceivedPoints {
        private final PointsExpression expression;
        private final Player player;
        private final BoardPointer source;

        public ReceivedPoints(PointsExpression expression, Player player, BoardPointer source) {
            this.expression = expression;
            this.player = player;
            this.source = source;
        }

        public int getPoints() {
            return expression.getPoints();
        }

        public PointsExpression getExpression() {
            return expression;
        }

        public Player getPlayer() {
            return player;
        }

        public BoardPointer getSource() {
            return source;
        }
    }

}
