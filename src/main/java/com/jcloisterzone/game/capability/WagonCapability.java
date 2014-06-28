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

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
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

    @Subscribe
    public void undeployed(MeepleEvent ev) {
        Meeple m = ev.getMeeple();
        if (m instanceof Wagon && ev.getTo() == null && game.getPhase() instanceof ScorePhase) {
        	returnedWagons.put(m.getPlayer(), getBoard().get(ev.getFrom()));
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
    public void turnPartCleanUp() {
        returnedWagons.clear();
        wagonPlayer = null;
    }

    private Set<FeaturePointer> filterWagonLocations(Set<FeaturePointer> followerOptions) {
        return Sets.filter(followerOptions, new Predicate<FeaturePointer>() {
            @Override
            public boolean apply(FeaturePointer bp) {
                Feature fe = getTile().getFeature(bp.getLocation());
                return fe instanceof Road || fe instanceof City || fe instanceof Cloister;
            }
        });
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (game.getActivePlayer().hasFollower(Wagon.class) && !followerOptions.isEmpty()) {
            Set<FeaturePointer> wagonLocations = filterWagonLocations(followerOptions);
            if (!wagonLocations.isEmpty()) {
                actions.add(new MeepleAction(Wagon.class).addAll(wagonLocations));
            }
        }
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
