package com.jcloisterzone.game.capability;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.GameExtension;

public class FairyCapability extends GameExtension {

    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    public Position fairyPosition;

    public Position getFairyPosition() {
        return fairyPosition;
    }

    public void setFairyPosition(Position fairyPosition) {
        this.fairyPosition = fairyPosition;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        FairyAction fairyAction = null;
        for(Follower m : game.getActivePlayer().getFollowers()) {
            if (m.getPosition() != null && ! m.getPosition().equals(fairyPosition)) {
                if (fairyAction == null) {
                    fairyAction = new FairyAction();
                    actions.add(fairyAction);
                }
                fairyAction.getSites().add(m.getPosition());
            }
        }
    }

    @Override
    public FairyCapability copy() {
        FairyCapability copy = new FairyCapability();
        copy.game = game;
        copy.fairyPosition = fairyPosition;
        return copy;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node, Expansion nodeFor) {
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
