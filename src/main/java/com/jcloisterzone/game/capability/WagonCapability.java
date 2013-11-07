package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.asLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.ScorePhase;

public class WagonCapability extends Capability {

    private final Map<Player, Feature> returnedWagons = new HashMap<>();
    private Player wagonPlayer;

    public WagonCapability(final Game game) {
        super(game);
    }

    @Override
    public void undeployed(Meeple m) {
        if (m instanceof Wagon && game.getPhase() instanceof ScorePhase) {
            returnedWagons.put(m.getPlayer(), m.getFeature());
        }
    }

    @Override
    public Object backup() {
        return new Object[] {
            wagonPlayer,
            new HashMap<>(returnedWagons)
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        wagonPlayer = (Player) a[0];
        returnedWagons.clear();
        returnedWagons.putAll((Map<Player, Feature>) a[1]);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Wagon(game, player));
    }

    public Map<Player, Feature> getReturnedWagons() {
        return returnedWagons;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("wagon-move");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            nl = ((Element) nl.item(0)).getElementsByTagName("neighbouring");
            for (int i = 0; i < nl.getLength(); i++) {
                processNeighbouringElement(tile, (Element) nl.item(i));
            }
        }
    }

    private void processNeighbouringElement(Tile tile, Element e) {
        String[] sides = asLocation(e);
        Feature[] te = new Feature[sides.length];
        for (int i = 0; i < te.length; i++) {
            te[i] = tile.getFeaturePartOf(Location.valueOf(sides[i]));
        }
        for (int i = 0; i < te.length; i++) {
            Feature[] neighbouring = new Feature[te.length - 1];
            int ni = 0;
            for (int j = 0; j < te.length; j++) {
                if (j == i)
                    continue;
                neighbouring[ni++] = te[j];
            }
            ((TileFeature) te[i]).addNeighbouring(neighbouring);
        }
    }

    @Override
    public void turnCleanUp() {
        returnedWagons.clear();
        wagonPlayer = null;
    }

    private Set<Location> copyWagonsLocations(Set<Location> locations) {
        Set<Location> result = new HashSet<>();
        for (Feature piece : getTile().getFeatures()) {
            Location loc = piece.getLocation();
            if (piece instanceof Road || piece instanceof City || piece instanceof Cloister) {
                if (locations.contains(loc)) {
                    result.add(loc);
                }
            }

        }
        return result;
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        Position pos = getTile().getPosition();
        Set<Location> tileLocations = followerLocMap.get(pos);
        if (game.getActivePlayer().hasFollower(Wagon.class)) {
            if (tileLocations != null) {
                Set<Location> wagonLocations = copyWagonsLocations(tileLocations);
                if (!wagonLocations.isEmpty()) {
                    actions.add(new MeepleAction(Wagon.class, pos, wagonLocations));
                }
            }
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        prepareFollowerActions(actions, commonSites);
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        for (Entry<Player, Feature> rv : returnedWagons.entrySet()) {
            Element el = doc.createElement("wagon");
            el.setAttribute("player", "" + rv.getKey().getIndex());
            el.setAttribute("loc", "" + rv.getValue().getLocation());
            XmlUtils.injectPosition(el, rv.getValue().getTile().getPosition());
            node.appendChild(el);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("wagon");
        for (int i = 0; i < nl.getLength(); i++) {
            Element wg = (Element) nl.item(i);
            Location loc = Location.valueOf(wg.getAttribute("loc"));
            Position pos = XmlUtils.extractPosition(wg);
            int playerIndex = Integer.parseInt(wg.getAttribute("player"));
            Player player = game.getPlayer(playerIndex);
            returnedWagons.put(player, getBoard().get(pos).getFeature(loc));
        }
    }

    public Player getWagonPlayer() {
        return wagonPlayer;
    }

    public void setWagonPlayer(Player wagonPlayer) {
        this.wagonPlayer = wagonPlayer;
    }

}
