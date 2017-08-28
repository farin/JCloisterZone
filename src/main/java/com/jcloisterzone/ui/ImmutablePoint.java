package com.jcloisterzone.ui;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Rotation;

@Immutable
public class ImmutablePoint {

    public static final ImmutablePoint ZERO = new ImmutablePoint(0, 0);

    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ImmutablePoint add(int tx, int ty) {
        return new ImmutablePoint(x + tx, y + ty);
    }

    public ImmutablePoint scale(int tileWidth, int tileHeight) {
        return scale(tileWidth, tileHeight, 0, 0);
    }

    public ImmutablePoint scale(int tileWidth, int tileHeight, int boxSize) {
        return scale(tileWidth, tileHeight, boxSize, boxSize);
    }

    public ImmutablePoint scale(int tileWidth, int tileHeight, int xSize, int ySize) {
        return new ImmutablePoint(
                (int) (tileWidth * (x / 100.0))
                    - xSize / 2,
                (int) (tileHeight * (y / 100.0))
                    - ySize / 2);
    }

    public ImmutablePoint mirrorX() {
        return new ImmutablePoint(100-x,y);
    }
    public ImmutablePoint mirrorY() {
        return new ImmutablePoint(x,100-y);
    }
    public ImmutablePoint rotate() {
        return new ImmutablePoint(100-y,x);
    }

    /**
     * Rotates ImmutablePoint on initial sized tile.
     * @param p ImmutablePoint to rotate
     * @param d how roatate
     * @return rotated ImmutablePoint
     */
    public ImmutablePoint rotate100(Rotation r) {
        int x = this.x, y = this.y, _y;
        for (int i = 0; i < r.ordinal(); i++) {
            _y = y;
            y = x;
            x  = 100-_y;
        }
        return new ImmutablePoint(x, y);
    }

    /**Rotate around [0,0]*/
    public ImmutablePoint rotate(Rotation r) {
        int x = this.x, y = this.y, _y;
        for (int i = 0; i < r.ordinal(); i++) {
            _y = y;
            y = x;
            x  = -_y;
        }
        return new ImmutablePoint(x, y);
    }

    public ImmutablePoint translate(int dx, int dy) {
        return new ImmutablePoint(x + dx, y + dy);
    }

    @Override
    public int hashCode() {
        return (x << 16) ^ y;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImmutablePoint) {
            ImmutablePoint p = (ImmutablePoint)obj;
            return (x == p.x) && (y == p.y);
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
}