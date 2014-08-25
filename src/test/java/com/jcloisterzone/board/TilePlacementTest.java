package com.jcloisterzone.board;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TilePlacementTest {

    @Test
    public void isPartOf() {
        TilePlacement[] arr = new TilePlacement[] {
            new TilePlacement(new Position(-4, 2), Rotation.R90),
            new TilePlacement(new Position(1, 1), Rotation.R180),
            new TilePlacement(new Position(0, 0), Rotation.R270),
            new TilePlacement(new Position(1, 1), Rotation.R0),
        };
        Arrays.sort(arr);
        assertEquals(new Position(0, 0), arr[0].getPosition());
        assertEquals(new Position(1, 1), arr[1].getPosition());
        assertEquals(Rotation.R0, arr[1].getRotation());
        assertEquals(new Position(1, 1), arr[2].getPosition());
        assertEquals(new Position(-4, 2), arr[3].getPosition());

    }


}
