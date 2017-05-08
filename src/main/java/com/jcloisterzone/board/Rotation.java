package com.jcloisterzone.board;

import java.awt.geom.AffineTransform;

public enum Rotation {

    R0,
    R90,
    R180,
    R270;

    public AffineTransform getAffineTransform(int size) {
        return getAffineTransform(size, size);
    }

    public AffineTransform getAffineTransform(int sizeX, int sizeY) {
        AffineTransform at;
        switch (this) {
        case R0:
            at = new AffineTransform();
            return at;
        case R90:
            at = AffineTransform.getRotateInstance(Math.PI * 0.5);
            at.translate(0, -sizeY);
            return at;
        case R180:
            at = AffineTransform.getRotateInstance(Math.PI);
            at.translate(-sizeX, -sizeY);
            return at;
        case R270:
            at = AffineTransform.getRotateInstance(Math.PI * 1.5);
            at.translate(-sizeX, 0);
            return at;
        }
        return null;
    }

    public Rotation next() {
        if (ordinal() == values().length - 1) return values()[0];
        return values()[ordinal()+1];
    }

    public Rotation prev() {
        if (ordinal() == 0) return values()[values().length - 1];
        return values()[ordinal()-1];
    }

    public double getTheta() {
        return ordinal()*Math.PI/2.0;
    }

    public Rotation add(Rotation r) {
        return Rotation.values()[(this.ordinal() + r.ordinal()) % Rotation.values().length];
    }

    public Rotation inverse() {
        switch (this) {
            case R0: return R0;
            case R90: return R270;
            case R180: return R180;
            case R270: return R90;
        }
        throw new IllegalStateException();
    }

}
