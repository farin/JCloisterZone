package com.jcloisterzone.board;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

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
    
    @Test
    public void testToString(){
    	TilePlacement[] arr = new TilePlacement[] {
                new TilePlacement(new Position(-4, 2), Rotation.R90),
                new TilePlacement(new Position(1, 1), Rotation.R180),
                new TilePlacement(new Position(0, 0), Rotation.R270),
                new TilePlacement(new Position(1, 1), Rotation.R0),
            };
    	Arrays.sort(arr);
    	String expected_one= "[x=0,y=0,R270]";
    	String expected_two= "[x=1,y=1,R0]"; // expected string values 
    	
    	assertEquals(expected_one,arr[0].toString());//comparing with actual string values using assertions
    	
    	assertEquals(expected_two,arr[1].toString());
    	
    	
    	
    }
    @Test
    public void testhashCode(){
    	TilePlacement testPlacement  = new TilePlacement(new Position(0, 0), Rotation.R270);
    	
        assertNotNull(testPlacement.hashCode());    
    	
    	
    }	
    
  	
    @Test
    public void testEquals(){
    	TilePlacement testPlacement  = new TilePlacement(new Position(0, 0), Rotation.R270);
    	TilePlacement testPlacement2  = new TilePlacement(new Position(1,1), Rotation.R270);
    	TilePlacement testPlacement3  = new TilePlacement(new Position(0, 0), Rotation.R0);
    	TilePlacement testPlacement4  = new TilePlacement(new Position(0, 0), Rotation.R270);
        assertNotNull(testPlacement.hashCode());    
    	assertFalse(testPlacement.equals(testPlacement2));
    	assertFalse(testPlacement.equals(testPlacement3));
    	assertTrue(testPlacement.equals(testPlacement4));
    	assertTrue(testPlacement.equals(testPlacement));
    	assertFalse(testPlacement.equals(null));
    	assertFalse(testPlacement.equals(1));
    	
    	
    
  	
    }
    
    
    

}
