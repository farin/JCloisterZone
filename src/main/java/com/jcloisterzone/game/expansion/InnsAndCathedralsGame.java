package com.jcloisterzone.game.expansion;

import static com.jcloisterzone.board.XmlUtils.attributeBoolValue;

import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.game.ExpandedGame;


public final class InnsAndCathedralsGame extends ExpandedGame {

	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
		if (feature instanceof City) {
			((City) feature).setCathedral(attributeBoolValue(xml, "cathedral"));
		}
		if (feature instanceof Road) {
			((Road) feature).setInn(attributeBoolValue(xml, "inn"));
		}
	}

	@Override
	public void initPlayer(Player player) {
		player.addMeeple(new BigFollower(game, player));
	}

	@Override
	public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
		if (game.getActivePlayer().hasFollower(BigFollower.class) && ! commonSites.isEmpty()) {
			actions.add(new MeepleAction(BigFollower.class, commonSites));
		}
	}

}
