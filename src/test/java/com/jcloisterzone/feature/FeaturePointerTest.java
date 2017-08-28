package com.jcloisterzone.feature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class FeaturePointerTest {

    @Test
    public void equals() {
        assertEquals(
            new FeaturePointer(Position.ZERO, Location.N),
            new FeaturePointer(Position.ZERO, Location.N)
        );
    }

    @Test
    public void rotateCW() {
        assertEquals(
            new FeaturePointer(Position.ZERO, Location.E),
            new FeaturePointer(Position.ZERO, Location.N).rotateCW(Rotation.R90)
        );
        assertEquals(
            new FeaturePointer(Position.ZERO, Location.S),
            new FeaturePointer(Position.ZERO, Location.W).rotateCW(Rotation.R270)
        );
    }

    @Test
    public void rotateCCW() {
        assertEquals(
            new FeaturePointer(Position.ZERO, Location.W),
            new FeaturePointer(Position.ZERO, Location.N).rotateCCW(Rotation.R90)
        );
        assertEquals(
            new FeaturePointer(Position.ZERO, Location.N),
            new FeaturePointer(Position.ZERO, Location.W).rotateCCW(Rotation.R270)
        );
    }

    @Test
    public void translate() {
        assertEquals(
            new FeaturePointer(new Position(-2, -3), Location.W),
            new FeaturePointer(Position.ZERO, Location.W).translate(new Position(-2, -3))
        );
    }


}