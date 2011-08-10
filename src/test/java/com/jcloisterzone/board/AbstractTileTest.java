package com.jcloisterzone.board;

import org.ini4j.Ini;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Game;

public class AbstractTileTest {

	Game game = new Game();
	TilePackFactory packFactory = new TilePackFactory();
	TileFactory tileFactory = new TileFactory();

	{
		game.setConfig(new Ini());
		game.getExpansions().add(Expansion.BASIC);
		packFactory.setGame(game);
		tileFactory.setGame(game);
	}

	protected Tile createTile(Expansion exp, String id) {
		Element el = packFactory.getExpansionDefinition(exp);
		NodeList nl = el.getElementsByTagName("card");
		for(int i = 0; i < nl.getLength(); i++) {
			Element card = (Element) nl.item(i);
			if (id.equals(card.getAttribute("id"))) {
				return tileFactory.createTile(id, card, false);
			}
		}
		throw new IllegalArgumentException("Invalid tile id");
	}

}
