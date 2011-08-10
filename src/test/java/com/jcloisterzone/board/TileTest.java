package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Road;

public class TileTest extends AbstractTileTest {

	Tile BA_RRR = createTile(Expansion.BASIC, "RRR");
	Tile BA_Cc1 = createTile(Expansion.BASIC, "Cc.1");
	Tile BA_Cc1_rot = createTile(Expansion.BASIC, "Cc.1");
	{
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
		assertEquals(Road.class, BA_RRR.getFeaturePartOf(Location.E, Road.class, City.class).getClass());
		assertEquals(City.class, BA_Cc1.getFeaturePartOf(Location.NW, Road.class, City.class).getClass());
		assertEquals(City.class, BA_Cc1.getFeaturePartOf(Location.N, Road.class, City.class).getClass());
		assertNull(BA_Cc1.getFeature(Location.E));

		assertNull(BA_Cc1_rot.getFeature(Location.W));
		assertEquals(City.class, BA_Cc1_rot.getFeaturePartOf(Location.N, Road.class, City.class).getClass());
		assertEquals(City.class, BA_Cc1_rot.getFeaturePartOf(Location.E, Road.class, City.class).getClass());
	}

//	@Test
//	public void getSideMaskAt() {
//		assertEquals('C', BA_Cc1_rot.getSideMaskAt(Location.N));
//		assertEquals('F', BA_Cc1_rot.getSideMaskAt(Location.S));
//	}

}
