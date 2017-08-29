package com.jcloisterzone.board;

import java.awt.geom.AffineTransform;


/**
 * Enumerates possible tile rotations.
 */
public enum Rotation {

    R0,
    R90,
    R180,
    R270;

    /**
     * Returns the affine transform associated with {@code this} rotation and scaled by {@code size}.
     *
     * @param size the scaling magnitude of the affine transform
     * @return the affine transform associated with {@code this} rotation and scaled by {@code size}
     */
    public AffineTransform getAffineTransform(int size) {
        return getAffineTransform(size, size);
    }

    /**
     * Returns the affine transform associated with {@code this} rotation and scaled by {@code sizeX} and {@code sizeY}.
     *
     * @param sizeX the scaling magnitude of the affine transform in the X direction
     * @param sizeY the scaling magnitude of the affine transform in the Y direction
     * @return the affine transform associated with {@code this} rotation and scaled by {@code sizeX} and {@code sizeY}
     */
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

    /**
     * Returns the next rotation clockwise (+90 degrees).
     *
     * @return the next rotation clockwise
     */
    public Rotation next() {
        return values()[(ordinal()+1) % values().length];
    }


    /**
     * Returns the previous rotation clockwise (-90 degrees).
     *
     * @return the previous rotation clockwise
     */
    public Rotation prev() {
        return values()[(ordinal()-1) % values().length];
    }

    /**
     * Returns the angle in radians associated with {@code this} rotation.
     *
     * @return the angle in radians associated with {@code this} rotation
     */
    public double getTheta() {
        return ordinal()*Math.PI/2.0;
    }

    /**
     * Adds rotation {@code r} to {@code this}.
     *
     * @param r the rotation to add
     * @return the sum of the rotations
     */
    public Rotation add(Rotation r) {
        return Rotation.values()[(this.ordinal() + r.ordinal()) % values().length];
    }

    /**
     * Returns the inverse of {@code this} rotation (+180 degrees).
     * @return  the inverse of {@code this} rotation
     */
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
