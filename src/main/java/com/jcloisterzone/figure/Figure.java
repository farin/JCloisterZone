package com.jcloisterzone.figure;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;

public abstract class Figure implements Serializable, Cloneable {

    private static final long serialVersionUID = 3264248810294656662L;

    protected final Game game;
    private Position position;

    public Figure(Game game) {
        assert game != null;
        this.game = game;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean at(Position p) {
        if (position == null) return false;
        return position.equals(p);
    }

    @Override
    public String toString() {
        if (position == null) {
            return getClass().getSimpleName();
        } else {
            return getClass().getSimpleName() + position.toString();
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (position == null ? 1 : position.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Figure)) return false;
        return Objects.equal(position, ((Figure) obj).position);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
