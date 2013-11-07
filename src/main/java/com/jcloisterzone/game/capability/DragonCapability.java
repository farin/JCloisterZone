package com.jcloisterzone.game.capability;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

public class DragonCapability extends Capability {

    public static final int DRAGON_MOVES = 6;

    public Position dragonPosition;
    public int dragonMovesLeft;
    public Player dragonPlayer;
    public Set<Position> dragonVisitedTiles;

    public DragonCapability(final Game game) {
        super(game);
    }

    @Override
    public void tilePlaced(Tile tile) {
        if (tile.hasTrigger(TileTrigger.VOLCANO)) {
            setDragonPosition(tile.getPosition());
            getTilePack().activateGroup("dragon");
            game.fireGameEvent().dragonMoved(tile.getPosition());
        }
    }

    @Override
    public Object backup() {
        return new Object[] {
            dragonPosition,
            dragonMovesLeft,
            dragonPlayer,
            dragonVisitedTiles == null ? null : new HashSet<>(dragonVisitedTiles)
         };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        dragonPosition = (Position) a[0];
        dragonMovesLeft = (Integer) a[1];
        dragonPlayer = (Player) a[2];
        dragonVisitedTiles = a[3] == null ? null : new HashSet<>((Set<Position>) a[3]);
    }


    @Override
    public String getTileGroup(Tile tile) {
        return tile.hasTrigger(TileTrigger.DRAGON) ? "dragon" : null;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("volcano").getLength() > 0) {
            tile.setTrigger(TileTrigger.VOLCANO);
        }
        if (xml.getElementsByTagName("dragon").getLength() > 0) {
            tile.setTrigger(TileTrigger.DRAGON);
        }
    }

    @Override
    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        if (!tile.getPosition().equals(dragonPosition)) return true;
        if (game.hasRule(CustomRule.CANNOT_PLACE_BUILDER_ON_VOLCANO)) return false;
        return Special.class.isAssignableFrom(meepleType);
    }

    public Position getDragonPosition() {
        return dragonPosition;
    }

    public void setDragonPosition(Position dragonPosition) {
        this.dragonPosition = dragonPosition;
    }

    public Player getDragonPlayer() {
        return dragonPlayer;
    }
    public void setDragonPlayer(Player dragonPlayer) {
        this.dragonPlayer = dragonPlayer;
    }

    public int getDragonMovesLeft() {
        return dragonMovesLeft;
    }

    public Set<Position> getDragonVisitedTiles() {
        return dragonVisitedTiles;
    }

    public void triggerDragonMove() {
        dragonMovesLeft = DRAGON_MOVES;
        dragonPlayer = game.getTurnPlayer();
        dragonVisitedTiles = new HashSet<>();
        dragonVisitedTiles.add(dragonPosition);
    }

    public void endDragonMove() {
        dragonMovesLeft = 0;
        dragonVisitedTiles = null;
        dragonPlayer = null;
    }

    public void moveDragon(Position p) {
        dragonVisitedTiles.add(p);
        dragonPosition = p;
        dragonPlayer = game.getNextPlayer(dragonPlayer);
        dragonMovesLeft--;
    }

    public Set<Position> getAvailDragonMoves() {
        Set<Position> result = new HashSet<>();
        FairyCapability fairyCap = game.getCapability(FairyCapability.class);
        for (Position offset: Position.ADJACENT.values()) {
            Position position = dragonPosition.add(offset);
            Tile tile = getBoard().get(position);
            if (tile == null || tile.getOrigin() == Expansion.COUNT) continue;
            if (dragonVisitedTiles != null && dragonVisitedTiles.contains(position)) { continue; }
            if (fairyCap != null && position.equals(fairyCap.getFairyPosition())) { continue; }
            result.add(position);
        }
        return result;
    }



    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (dragonPosition != null) {
            Element dragon = doc.createElement("dragon");
            XmlUtils.injectPosition(dragon, dragonPosition);
            if (dragonMovesLeft > 0) {
                dragon.setAttribute("moves", "" + dragonMovesLeft);
                dragon.setAttribute("movingPlayer", "" + dragonPlayer.getIndex());
                if (dragonVisitedTiles != null) {
                    for (Position visited : dragonVisitedTiles) {
                        Element ve = doc.createElement("visited");
                        XmlUtils.injectPosition(ve, visited);
                        dragon.appendChild(ve);
                    }
                }
            }
            node.appendChild(dragon);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("dragon");
        if (nl.getLength() > 0) {
            Element dragon = (Element) nl.item(0);
            dragonPosition = XmlUtils.extractPosition(dragon);
            game.fireGameEvent().dragonMoved(dragonPosition);
            if (dragon.hasAttribute("moves")) {
                dragonMovesLeft  = Integer.parseInt(dragon.getAttribute("moves"));
                dragonPlayer = game.getPlayer(Integer.parseInt(dragon.getAttribute("movingPlayer")));
                dragonVisitedTiles = new HashSet<>();
                NodeList vl = dragon.getElementsByTagName("visited");
                for (int i = 0; i < vl.getLength(); i++) {
                    dragonVisitedTiles.add(XmlUtils.extractPosition((Element) vl.item(i)));
                }
            }
        }
    }


}
