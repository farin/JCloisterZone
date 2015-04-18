package com.jcloisterzone.game.capability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.LittleBuildingEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class LittleBuildingsCapability extends Capability {

    @SuppressWarnings("unchecked")
    private final Map<Player, Integer>[] buildings = new Map[LittleBuilding.values().length];
    private final Map<Position, LittleBuilding> placedBuildings = new HashMap<Position, LittleBuilding>();

    public LittleBuildingsCapability(Game game) {
        super(game);
        for (int i = 0; i < buildings.length; i++) {
            buildings[i] = new HashMap<>();
        }
    }

    @Override
    public Object backup() {
        Object[] a = new Object[2];
        a[0] = new Map[buildings.length];
        for (int i = 0; i < buildings.length; i++) {
            ((Map[])a[0])[i] = new HashMap<>(buildings[i]);
        }
        a[1] = new HashMap<>(placedBuildings);
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        Map<Player, Integer>[] buildingsBackup = (Map<Player, Integer>[]) a[0];
        for (int i = 0; i < buildings.length; i++) {
            buildings[i].clear();
            buildings[i].putAll(buildingsBackup[i]);
        }
        placedBuildings.clear();
        placedBuildings.putAll((Map<Position, LittleBuilding>) a[1]);
    }

    @Override
    public void initPlayer(Player player) {
        int playerCount = game.getAllPlayers().length;
        for (int i = 0; i < buildings.length; i++) {
            buildings[i].put(player, 6 / playerCount);
        }
    }

    public int getBuildingsCount(Player player, LittleBuilding lbType) {
        return buildings[lbType.ordinal()].get(player);
    }

    public void  setBuildingsCount(Player player, LittleBuilding lbType, int count) {
        buildings[lbType.ordinal()].put(player, count);
    }

    public void placeLittleBuilding(Player player, LittleBuilding lbType) {
        int buildingCount = getBuildingsCount(player, lbType);
        if (buildingCount == 0) {
            throw new IllegalStateException("Player hasn't " + lbType.name() + " building.");
        }
        setBuildingsCount(player, lbType, buildingCount - 1);
        Position pos = getCurrentTile().getPosition();
        placedBuildings.put(pos, lbType);
        game.post(new LittleBuildingEvent(player, lbType, pos));
    }

    public LittleBuilding getPlacedLittleBuilding(Position pos) {
    	return placedBuildings.get(pos);
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        Player player = game.getActivePlayer();
        LittleBuildingAction action = new LittleBuildingAction();
        for (LittleBuilding lb : LittleBuilding.values()) {
            if (getBuildingsCount(player, lb) > 0) {
                action.add(lb);
            }
        }
        if (!action.isEmpty()) {
            actions.add(action);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            for (LittleBuilding lb : LittleBuilding.values()) {
                el.setAttribute("lb-" + lb.name(), "" + getBuildingsCount(player, lb));
            }
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            for (LittleBuilding lb : LittleBuilding.values()) {
                int value = Integer.parseInt(playerEl.getAttribute("lb-" + lb.name()));
                setBuildingsCount(player, lb, value);
            }
        }
    }

    @Override
    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
    	LittleBuilding lb = placedBuildings.get(tile.getPosition());
    	if (lb != null) {
    		tileNode.setAttribute("littleBuilding", lb.name());
    	}
    }

    @Override
    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
    	if (tileNode.hasAttribute("littleBuilding")) {
            LittleBuilding lb =  LittleBuilding.valueOf(tileNode.getAttribute("littleBuilding"));
            placedBuildings.put(tile.getPosition(), lb);
        }
    }
}
