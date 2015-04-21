package com.jcloisterzone.feature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;

public class CloisterScoring extends AbstractScoringTest {
	
	protected void assertScore(int expected, Tile tile) {
		Cloister cl = (Cloister) tile.getCloister();		
		CompletableScoreContext ctx = cl.getScoreContext();
		cl.walk(ctx);
		
		assertEquals(expected, ctx.getPoints());
	}
	
	@Test
	public void single() {		
		Tile t; 
		t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "L");		
		
		assertScore(1, t);		
	}
	
	@Test
	public void incomplete() {		
		Tile t; 
		t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "L");		
		putTile(new Position(1,0), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(1,1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(0,1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(-1,0), Rotation.R0, Expansion.BASIC, "L");
		
		assertScore(5, t);		
	}
	
	@Test
	public void completed() {		
		Tile t; 
		t = putTile(new Position(0,0), Rotation.R0, Expansion.BASIC, "LR");		
		putTile(new Position(1,0), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(1,1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(0,1), Rotation.R90, Expansion.BASIC, "RFr");
		putTile(new Position(-1,0), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(-1,-1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(0,-1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(1,-1), Rotation.R0, Expansion.BASIC, "L");
		putTile(new Position(-1,1), Rotation.R0, Expansion.BASIC, "L");
		
		assertScore(9, t);		
	}

}
