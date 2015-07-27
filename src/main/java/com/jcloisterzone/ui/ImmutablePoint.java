package com.jcloisterzone.ui;

import com.jcloisterzone.board.Rotation;

//import java.util.WeakHashMap;

public class ImmutablePoint {

    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /*static public ImmutablePoint newInstance(int x, int y) {
        return new ImmutablePoint(x,y);
    }*/

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ImmutablePoint scale(int squareSize) {
        return scale(squareSize, 0, 0);
    }

    public ImmutablePoint scale(int squareSize, int boxSize) {
        return scale(squareSize, boxSize,boxSize);
    }

    public ImmutablePoint scale(int squareSize, int xSize, int ySize) {
        return new ImmutablePoint(
                (int) (squareSize * (x / 100.0))
                    - xSize / 2,

                (int) (squareSize * (y / 100.0))
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

    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
}