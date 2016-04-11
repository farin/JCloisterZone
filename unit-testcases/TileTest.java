package com.jcloisterzone.board;

import org.junit.Before;
import org.junit.Test;

import com.jcloisterzone.Expansion;
//import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
//import com.jcloisterzone.game.PlayerSlot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;

public class TileTest extends AbstractTileTest {

    Tile BA_RRR, BA_Cc1, BA_Cc1_rot;


    @Before
    public void initTiles() {
        BA_RRR = createTile(Expansion.BASIC, "RRR");
        BA_Cc1 = createTile(Expansion.BASIC, "Cc.1");
        BA_Cc1_rot = createTile(Expansion.BASIC, "Cc.1");
        BA_Cc1_rot.setRotation(Rotation.R90);
    }


    @Test
    public void getPiece() {
        assertEquals(Road.class, BA_RRR.getFeature(Location.E).getClass());
        assertNull(BA_RRR.getFeature(Location.N));
        assertEquals(City.class, BA_Cc1.getFeature(Location.NW).getClass());
        assertNull(BA_Cc1.getFeature(Location.N));

        assertEquals(City.class, BA_Cc1_rot.getFeature(Location.NE).getClass());
        assertNull(BA_Cc1_rot.getFeature(Location.NW));
    }

    @Test
    public void getPiecePartOf() {
        assertEquals(Road.class, BA_RRR.getFeaturePartOf(Location.E).getClass());
        assertEquals(City.class, BA_Cc1.getFeaturePartOf(Location.NW).getClass());
        assertEquals(City.class, BA_Cc1.getFeaturePartOf(Location.N).getClass());
        assertNull(BA_Cc1.getFeature(Location.E));

        assertNull(BA_Cc1_rot.getFeature(Location.W));
        assertEquals(City.class, BA_Cc1_rot.getFeaturePartOf(Location.N).getClass());
        assertEquals(City.class, BA_Cc1_rot.getFeaturePartOf(Location.E).getClass());
    }
    
    @Test
    public void testGetOrigin(){
    
    
    	assertEquals("Basic game",BA_RRR.getOrigin().toString());// comparing as string
    	assertEquals(BA_Cc1.getOrigin(),BA_RRR.getOrigin());// comparing as objects to solidify the test case
    	
    }

    @Test
    public void testGetTileSymmetry(){
    	
    	BA_RRR.setSymmetry(TileSymmetry.S2);
    	assertEquals(TileSymmetry.S2,BA_RRR.getSymmetry());
    	
    
    	}
    
    @Test
    public void testGetUnoccupiedScorables(){
    	assertEquals(BA_Cc1.getUnoccupiedScoreables(true),BA_Cc1.getUnoccupiedScoreables(false));
    	// we know the output has to be same irrespective of the fact that that the condition in parameter is true or false
    	// we used this functionality to make the assertion 
    	
    	
    }
    @Test
    public void testGetCityWithPrincess(){ 
    	assertNull(BA_RRR.getCityWithPrincess()); // covering  one branch
    	
    	
    	assertNull(BA_Cc1_rot.getCityWithPrincess()); // covering other side of if loop
    	
    }
    
}
