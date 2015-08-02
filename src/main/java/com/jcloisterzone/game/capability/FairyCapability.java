package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

public class FairyCapability extends Capability {

    public static final int FAIRY_POINTS_BEGINNING_OF_TURN = 1;
    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    public final Fairy fairy;

    public FairyCapability(Game game) {
        super(game);
        fairy = new Fairy(game);
        game.getNeutralFigures().add(fairy);
    }

    @Override
    public void handleEvent(Event event) {
       if (event instanceof MeepleEvent) {
           undeployed((MeepleEvent) event);
       }

    }

    private void undeployed(MeepleEvent ev) {
        if (ev.getFrom() == null) return;
        if (ev.getMeeple() == fairy.getNextTo()) {
            fairy.setNextTo(null);
        }
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
        if (game.getBooleanValue(CustomRule.FAIRY_ON_TILE)) {
            Position pos = f.getPosition();
            return pos != null && pos.equals(fairy.getPosition());
        } else {
            return fairy.getNextTo() == f && f.at(fairy.getFeaturePointer());
        }
    }

    public List<Follower> getFollowersNextToFairy() {
        if (fairy.getFeaturePointer() == null) {
            return Collections.emptyList();
        }
        List<Follower> result = new ArrayList<>();
        for (Meeple m : game.getDeployedMeeples()) {
            if (m instanceof Follower) {
                Follower f = (Follower) m;
                if (isNextTo(f)) {
                    result.add(f);
                }
            }
        }
        return result;
    }


    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        boolean fairyOnTile = game.getBooleanValue(CustomRule.FAIRY_ON_TILE);
        Player activePlayer = game.getActivePlayer();
        PlayerAction<?> fairyAction;
        if (fairyOnTile) {
            fairyAction = new FairyOnTileAction();
        } else {
            fairyAction = new FairyNextToAction();
        }

        for (Follower m : Iterables.filter(activePlayer.getFollowers(), MeeplePredicates.deployed())) {
            if (fairyOnTile) {
                if (!m.at(fairy.getPosition())) {
                    ((FairyOnTileAction) fairyAction).add(m.getPosition());
                }
            } else {
                if (!m.equals(fairy.getNextTo())) {
                    ((FairyNextToAction) fairyAction).add(new MeeplePointer(m));
                }
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
            if (fairy.getNextTo() != null) {
                fairyEl.setAttribute("next-to", fairy.getNextTo().getId());
            }
            node.appendChild(fairyEl);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("fairy");
        if (nl.getLength() > 0) {
            Element fairyEl = (Element) nl.item(0);
            FeaturePointer fp = XMLUtils.extractFeaturePointer(fairyEl);
            String nextTo = fairyEl.getAttribute("next-to");
            if (nextTo != null) {
                MeeplePointer mp = new MeeplePointer(fp.getPosition(), fp.getLocation(), nextTo);
                fairy.deploy(mp);
            } else {
                fairy.deploy(fp.getPosition());
            }
        }
    }
}
