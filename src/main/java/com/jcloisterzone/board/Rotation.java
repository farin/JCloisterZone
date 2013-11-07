package com.jcloisterzone.board;

import java.awt.geom.AffineTransform;

public enum Rotation {

    R0,
    R90,
    R180,
    R270;

    public AffineTransform getAffineTransform(int size) {
        AffineTransform at;
        switch (this) {
        case R0:
            at = new AffineTransform();
            return at;
        case R90:
            at = AffineTransform.getRotateInstance(Math.PI * 0.5);
            at.translate(0, -size);
            return at;
        case R180:
            at = AffineTransform.getRotateInstance(Math.PI);
            at.translate(-size, -size);
            return at;
        case R270:
            at = AffineTransform.getRotateInstance(Math.PI * 1.5);
            at.translate(-size, 0);
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
