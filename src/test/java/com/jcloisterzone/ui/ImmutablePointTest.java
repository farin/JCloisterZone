package com.jcloisterzone.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImmutablePointTest {

    @Test
    public void scale() {
        ImmutablePoint p = new ImmutablePoint(50, 1);
        assertEquals(100, p.scale(200).getX());
        assertEquals(25, p.scale(50).getX());

        p = new ImmutablePoint(10, 1);
        assertEquals(10, p.scale(100).getX());
        assertEquals(8, p.scale(80).getX());
    }

}
