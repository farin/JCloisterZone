package com.jcloisterzone.board;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.Expansion;

public class TileFactoryTest extends AbstractTileTest {

	Tile BA_RRR = createTile(Expansion.BASIC, "RRR");
	Tile BA_Cc1 = createTile(Expansion.BASIC, "Cc.1");

//	@Test
//	public void createSideMaskFor() {
//		assertEquals("FRRR", tileFactory.createSideMaskFor(BA_RRR));
//		assertEquals("CFFC", tileFactory.createSideMaskFor(BA_Cc1));
//	}

}
