package com.jcloisterzone.event.play;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import io.vavr.collection.List;

@Immutable
public class ScoreEvent extends PlayEvent  {

    private static final long serialVersionUID = 1L;

    private final List<ReceivedPoints> points;
    private final String category;
    private final boolean landscapeSource;
    private final boolean isFinal;

    public ScoreEvent(List<ReceivedPoints> points, String category, boolean landscapeSource, boolean isFinal) {
        super(PlayEventMeta.createWithoutPlayer());
        this.points = points;
        this.category = category;
        this.landscapeSource = landscapeSource;
        this.isFinal = isFinal;
    }

    public ScoreEvent(ReceivedPoints points, String category, boolean landscapeSource, boolean isFinal) {
        this(List.of(points), category, landscapeSource, isFinal);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<ReceivedPoints> getPoints() {
        return points;
    }

    public String getCategory() {
        return category;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isLandscapeSource() {
        return landscapeSource;
    }

    @Immutable
    public static class ReceivedPoints {
        private final int points;
        private final String expression;
        private final Player receiver;
        private final BoardPointer source;

        public ReceivedPoints(int points, String expression, Player receiver, BoardPointer source) {
            this.points = points;
            this.expression = expression;
            this.receiver = receiver;
            this.source = source;
        }

        public int getPoints() {
            return points;
        }

        public String getExpression() {
            return expression;
        }

        public Player getReceiver() {
            return receiver;
        }

        public BoardPointer getSource() {
            return source;
        }
    }

}
