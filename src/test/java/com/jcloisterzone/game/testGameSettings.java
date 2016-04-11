package com.jcloisterzone.game;

import static org.junit.Assert.*;

import org.junit.Test;

public class testGameSettings {
	
	GameSettings gameObj = new GameSettings("testID");
	@Test
	public void testGetGameID(){
		assertEquals( "testID",gameObj.getGameId());
		
	}
	
	@Test
	public void testgetName(){
		gameObj.setName("testName");
		assertEquals("testName",gameObj.getName());
	}
}
