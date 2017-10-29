package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

public class ScoreEvent extends PlayEvent  {

    private static final long serialVersionUID = 1L;

    private final int points;
    private final String label;
    private final PointCategory category;
    private final boolean isFinal;

    private final BoardPointer pointer;
    private final Meeple meeple;
    private final Player receiver;


    public ScoreEvent(int points, String label, PointCategory category, boolean isFinal,
            BoardPointer pointer, Meeple meeple, Player receiver) {
        super(PlayEventMeta.createWithoutPlayer());
        this.points = points;
        this.category = category;
        this.label = label;
        this.isFinal = isFinal;
        this.pointer = pointer;
        this.meeple = meeple;
        this.receiver = receiver;
    }

    public ScoreEvent(
        int points, PointCategory category, boolean isFinal,
        FeaturePointer fp, Meeple meeple
    ) {
        this(
            points, points + "", category, isFinal,
            fp, meeple, meeple.getPlayer()
        );
    }

    public ScoreEvent(
        int points, String label, PointCategory category, boolean isFinal,
        FeaturePointer fp, Meeple meeple
    ) {
        this(
            points, label, category, isFinal,
            fp, meeple, meeple.getPlayer()
        );
    }

    public ScoreEvent(
        int points, String label, PointCategory category, boolean isFinal,
        Position position, Player receiver
    ) {
        this(
            points, label, category, isFinal,
            position, null, receiver
        );
    }

    public FeaturePointer getFeaturePointer() {
        return (pointer instanceof FeaturePointer) ? (FeaturePointer) pointer : null;
    }

    public Position getPosition() {
        return pointer.getPosition();
    }

    public int getPoints() {
        return points;
    }

    public String getLabel() {
        return label == null ? points + "" : label;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public PointCategory getCategory() {
        return category;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Player getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return "ScoreEvent(  " + pointer +  ", " + points + ")";
    }
}
