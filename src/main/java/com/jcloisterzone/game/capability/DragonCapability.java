package com.jcloisterzone.game.capability;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class DragonCapability extends Capability {

    public static final int DRAGON_MOVES = 6;

    private final Dragon dragon;
    private int dragonMovesLeft;
    private Player dragonPlayer;
    private Set<Position> dragonVisitedTiles;

    public DragonCapability(final Game game) {
        super(game);
        dragon = new Dragon(game);
    }

    @Override
    public void handleEvent(Event event) {
       if (event instanceof TileEvent) {
           tilePlaced((TileEvent) event);
       }

    }

    private void tilePlaced(TileEvent ev) {
        Tile tile = ev.getTile();
        if (ev.getType() == TileEvent.PLACEMENT && tile.hasTrigger(TileTrigger.VOLCANO)) {
            getTilePack().setGroupState("dragon", TileGroupState.ACTIVE);
            dragon.deploy(tile.getPosition().asFeaturePointer());
        }
    }

    @Override
    public Object backup() {
        return new Object[] {
            dragon.getPosition(),
            dragonMovesLeft,
            dragonPlayer,
            dragonVisitedTiles == null ? null : new HashSet<>(dragonVisitedTiles)
         };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        dragon.setFeaturePointer(((Position) a[0]).asFeaturePointer());
        dragonMovesLeft = (Integer) a[1];
        dragonPlayer = (Player) a[2];
        dragonVisitedTiles = a[3] == null ? null : new HashSet<>((Set<Position>) a[3]);
    }

    public Dragon getDragon() {
		return dragon;
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
    	return !dragon.at(tile.getPosition());
    }

    public Player getDragonPlayer() {
        return dragonPlayer;
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
        dragonVisitedTiles.add(dragon.getPosition());
    }

    public void endDragonMove() {
        dragonMovesLeft = 0;
        dragonVisitedTiles = null;
        dragonPlayer = null;
    }

    public void moveDragon(Position p) {
        dragonVisitedTiles.add(p);
        dragonPlayer = game.getNextPlayer(dragonPlayer);
        dragonMovesLeft--;
        dragon.deploy(p.asFeaturePointer());
    }

    public Set<Position> getAvailDragonMoves() {
        Set<Position> result = new HashSet<>();
        FairyCapability fairyCap = game.getCapability(FairyCapability.class);
        for (Position offset: Position.ADJACENT.values()) {
            Position position = dragon.getPosition().add(offset);
            Tile tile = getBoard().get(position);
            if (tile == null || CountCapability.isTileForbidden(tile)) continue;
            if (dragonVisitedTiles != null && dragonVisitedTiles.contains(position)) { continue; }
            if (fairyCap != null && position.equals(fairyCap.getFairy().getPosition())) { continue; }
            result.add(position);
        }
        return result;
    }



    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (dragon.isDeployed()) {
            Element dragonEl = doc.createElement("dragon");
            XMLUtils.injectPosition(dragonEl, dragon.getPosition());
            if (dragonMovesLeft > 0) {
                dragonEl.setAttribute("moves", "" + dragonMovesLeft);
                dragonEl.setAttribute("movingPlayer", "" + dragonPlayer.getIndex());
                if (dragonVisitedTiles != null) {
                    for (Position visited : dragonVisitedTiles) {
                        Element ve = doc.createElement("visited");
                        XMLUtils.injectPosition(ve, visited);
                        dragonEl.appendChild(ve);
                    }
                }
            }
            node.appendChild(dragonEl);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("dragon");
        if (nl.getLength() > 0) {
            Element dragonEl = (Element) nl.item(0);
            dragon.deploy(XMLUtils.extractPosition(dragonEl).asFeaturePointer());
            if (dragonEl.hasAttribute("moves")) {
                dragonMovesLeft  = Integer.parseInt(dragonEl.getAttribute("moves"));
                dragonPlayer = game.getPlayer(Integer.parseInt(dragonEl.getAttribute("movingPlayer")));
                dragonVisitedTiles = new HashSet<>();
                NodeList vl = dragonEl.getElementsByTagName("visited");
                for (int i = 0; i < vl.getLength(); i++) {
                    dragonVisitedTiles.add(XMLUtils.extractPosition((Element) vl.item(i)));
                }
            }
        }
    }


}
