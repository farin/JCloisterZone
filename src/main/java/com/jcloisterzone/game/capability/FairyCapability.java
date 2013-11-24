package com.jcloisterzone.game.capability;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class FairyCapability extends Capability {

    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    public Position fairyPosition;

    public FairyCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return fairyPosition;
    }

    @Override
    public void restore(Object data) {
        fairyPosition = (Position) data;
    }

    public Position getFairyPosition() {
        return fairyPosition;
    }

    public void setFairyPosition(Position fairyPosition) {
        this.fairyPosition = fairyPosition;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        FairyAction fairyAction = new FairyAction();
        Player activePlayer = game.getActivePlayer();
        for (Follower m : Iterables.filter(activePlayer.getFollowers(), MeeplePredicates.deployed())) {
            if (!m.at(fairyPosition)) {
                fairyAction.getSites().add(m.getPosition());
            }
        }
        if (!fairyAction.getSites().isEmpty()) {
            actions.add(fairyAction);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (fairyPosition != null) {
            Element fairy = doc.createElement("fairy");
            XmlUtils.injectPosition(fairy, fairyPosition);
            node.appendChild(fairy);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("fairy");
        if (nl.getLength() > 0) {
            Element fairy = (Element) nl.item(0);
            fairyPosition = XmlUtils.extractPosition(fairy);
            game.fireGameEvent().fairyMoved(fairyPosition);
        }
    }
}
