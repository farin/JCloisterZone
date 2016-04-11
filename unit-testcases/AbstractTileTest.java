package com.jcloisterzone.board;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.Game;

public class AbstractTileTest {

    protected Game game = new Game("12345678");
    protected TilePackFactory packFactory = new TilePackFactory();
    protected TileFactory tileFactory = new TileFactory();

    protected void setUpGame(Game game) {
        game.getExpansions().add(Expansion.BASIC);
    }

    @Before
    public void initFactories() {
        Config config = new Config();
        setUpGame(game);
        packFactory.setGame(game);
        packFactory.setConfig(config);
        tileFactory.setGame(game);
    }

    protected Tile createTile(Expansion exp, String id) {
        Element el = packFactory.getExpansionDefinition(exp);
        NodeList nl = el.getElementsByTagName("tile");
        for (int i = 0; i < nl.getLength(); i++) {
            Element card = (Element) nl.item(i);
            if (id.equals(card.getAttribute("id"))) {
                return tileFactory.createTile(exp, id, card, false);
            }
        }
        throw new IllegalArgumentException("Invalid tile id");
    }




	@Test
	public void TestGetGame(){
		assertEquals(game,packFactory.getGame());
		
	}
	@Test
	public void testGetExpansionTiles(){
		assertNotNull(packFactory.getExpansionTiles(Expansion.BASIC));
		assertNotNull(packFactory.getExpansionTiles(Expansion.CULT));
		assertNotNull(packFactory.getExpansionTiles(Expansion.COUNT));
		
	}
	
	@Test
	public void testGetExpansionSize(){
		
		int expected[] ={6,72,12};
	
		assertEquals(expected[0],packFactory.getExpansionSize(Expansion.CULT));
		assertEquals(expected[1],packFactory.getExpansionSize(Expansion.BASIC));
		assertEquals(expected[2],packFactory.getExpansionSize(Expansion.COUNT));
		
	}
	
	
}