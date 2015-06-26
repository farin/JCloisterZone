package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class FairyCapability extends Capability {

    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    public final Fairy fairy;

    public FairyCapability(Game game) {
        super(game);
        fairy = new Fairy(game);
        game.getNeutralFigures().add(fairy);
    }

    @Override
    public Object backup() {
    	return fairy.getFeaturePointer();
    }

    @Override
    public void restore(Object data) {
    	fairy.setFeaturePointer((FeaturePointer) data);
    }

    public Fairy getFairy() {
		return fairy;
	}

    public boolean isNextTo(Follower f) {
    	Position pos = f.getPosition();
    	return pos != null && pos.equals(fairy.getPosition());
    }


    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        FairyAction fairyAction = new FairyAction();
        Player activePlayer = game.getActivePlayer();
        for (Follower m : Iterables.filter(activePlayer.getFollowers(), MeeplePredicates.deployed())) {
            if (!m.at(fairy.getPosition())) {
                fairyAction.add(m.getPosition());
            }
        }
        if (!fairyAction.isEmpty()) {
            actions.add(fairyAction);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (fairy.isDeployed()) {
            Element fairyEl = doc.createElement("fairy");
            XMLUtils.injectFeaturePoiner(fairyEl, fairy.getFeaturePointer());
            node.appendChild(fairyEl);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("fairy");
        if (nl.getLength() > 0) {
            Element fairyEl = (Element) nl.item(0);
            fairy.deploy(XMLUtils.extractFeaturePointer(fairyEl));
        }
    }
}
