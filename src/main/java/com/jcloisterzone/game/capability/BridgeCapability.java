package com.jcloisterzone.game.capability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BridgeCapability extends Capability {

    private boolean bridgeUsed;
    private final Map<Player, Integer> bridges = new HashMap<>();

    public BridgeCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return new Object[] {
            bridgeUsed,
            new HashMap<>(bridges)
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        bridgeUsed = (Boolean) a[0];
        bridges.clear();
        bridges.putAll((Map<Player, Integer>) a[1]);
    }


    @Override
    public void initPlayer(Player player) {
        int players = game.getAllPlayers().length;
        if (players < 5) {
            bridges.put(player, 3);
        } else {
            bridges.put(player, 2);
        }
    }

    @Override
    public void turnCleanUp() {
        bridgeUsed = false;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        if (!bridgeUsed && getPlayerBridges(game.getPhase().getActivePlayer()) > 0) {
            BridgeAction action = prepareBridgeAction();
            if (action != null) {
                actions.add(action);
            }
        }
    }

    public BridgeAction prepareMandatoryBridgeAction() {
        Tile tile = game.getCurrentTile();
        for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            Tile adjacent = entry.getValue();
            Location rel = entry.getKey();

            char adjacentSide = adjacent.getEdge(rel.rev());
            char tileSide = tile.getEdge(rel);
            if (tileSide != adjacentSide) {
                Location bridgeLoc = getBridgeLocationForAdjacent(rel);
                BridgeAction action = prepareTileBridgeAction(tile, null, bridgeLoc);
                if (action != null) return action;
                return prepareTileBridgeAction(adjacent, null, bridgeLoc);
            }
        }
        throw new IllegalStateException();
    }

    private Location getBridgeLocationForAdjacent(Location rel) {
        if (rel == Location.N || rel == Location.S) {
            return Location.NS;
        } else {
            return Location.WE;
        }
    }

    private BridgeAction prepareBridgeAction() {
        BridgeAction action = null;
        Tile tile = game.getCurrentTile();
        action = prepareTileBridgeAction(tile, action, Location.NS);
        action = prepareTileBridgeAction(tile, action, Location.WE);
        for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            Tile adjacent = entry.getValue();
            Location rel = entry.getKey();
            action = prepareTileBridgeAction(adjacent, action, getBridgeLocationForAdjacent(rel));
        }
        return action;
    }

    private BridgeAction prepareTileBridgeAction(Tile tile, BridgeAction action, Location bridgeLoc) {
        if (isBridgePlacementAllowed(tile, tile.getPosition(), bridgeLoc)) {
            if (action == null) action = new BridgeAction();
            action.getLocationsMap().getOrCreate(tile.getPosition()).add(bridgeLoc);
        }
        return action;
    }

    private boolean isBridgePlacementAllowed(Tile tile, Position p, Location bridgeLoc) {
        if (!tile.isBridgeAllowed(bridgeLoc)) return false;
        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
            Location rel = e.getKey();
            if (rel.intersect(bridgeLoc) != null) {
                Tile adjacent = e.getValue();
                char adjacentSide = adjacent.getEdge(rel.rev());
                if (adjacentSide != 'R') return false;
            }
        }
        return true;
    }

    public boolean isTilePlacementWithBridgePossible(Tile tile, Position p) {
        if (getPlayerBridges(game.getActivePlayer()) > 0) {
            if (isTilePlacementWithBridgeAllowed(tile, p, Location.NS)) return true;
            if (isTilePlacementWithBridgeAllowed(tile, p, Location.WE)) return true;
            if (isTilePlacementWithOneAdjacentBridgeAllowed(tile, p)) return true;
        }
        return false;
    }

    private boolean isTilePlacementWithBridgeAllowed(Tile tile, Position p, Location bridgeLoc) {
        if (!tile.isBridgeAllowed(bridgeLoc)) return false;

        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
            Tile adjacent = e.getValue();
            Location rel = e.getKey();

            char adjacentSide = adjacent.getEdge(rel.rev());
            char tileSide = tile.getEdge(rel);
            if (rel.intersect(bridgeLoc) != null) {
                if (adjacentSide != 'R') return false;
            } else {
                if (adjacentSide != tileSide) return false;
            }
        }
        return true;
    }

    private boolean isTilePlacementWithOneAdjacentBridgeAllowed(Tile tile, Position p) {
        boolean bridgeUsed = false;
        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
            Tile adjacent = e.getValue();
            Location rel = e.getKey();

            char tileSide = tile.getEdge(rel);
            char adjacentSide = adjacent.getEdge(rel.rev());

            if (tileSide != adjacentSide) {
                if (bridgeUsed) return false;
                if (tileSide != 'R') return false;

                Location bridgeLoc = getBridgeLocationForAdjacent(rel);
                if (!isBridgePlacementAllowed(adjacent, adjacent.getPosition(), bridgeLoc)) return false;
                bridgeUsed = true;
            }
        }
        return bridgeUsed; //ok if exactly one bridge is used
    }

    public int getPlayerBridges(Player pl) {
        return bridges.get(pl);
    }

    public void decreaseBridges(Player player) {
        int n = getPlayerBridges(player);
        if (n == 0) throw new IllegalStateException("Player has no bridges");
        bridges.put(player, n-1);
    }

    public void deployBridge(Position pos, Location loc) {
        Tile tile = getBoard().get(pos);
        if (!tile.isBridgeAllowed(loc)) {
            throw new IllegalArgumentException("Cannot deploy " + loc + " bridge on " + pos);
        }
        bridgeUsed = true;
        tile.placeBridge(loc);
        game.fireGameEvent().bridgeDeployed(pos, loc);
    }



    @Override
    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
        if (tile.getBridge() != null) {
            Location realLoc = tile.getBridge().getRawLocation();
            tileNode.setAttribute("bridge", realLoc.toString());
        }
    }

    @Override
    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
        if (tileNode.hasAttribute("bridge")) {
            Location loc =  Location.valueOf(tileNode.getAttribute("bridge"));
            tile.placeBridge(loc);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        node.setAttribute("bridgeUsed", bridgeUsed + "");
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("bridges", "" + getPlayerBridges(player));
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        bridgeUsed = Boolean.parseBoolean(node.getAttribute("bridgeUsed"));
        NodeList nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            bridges.put(player, Integer.parseInt(playerEl.getAttribute("bridges")));
        }
    }


}
