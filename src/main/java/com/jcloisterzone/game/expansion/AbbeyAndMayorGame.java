package com.jcloisterzone.game.expansion;

import static com.jcloisterzone.XmlUtils.asLocation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.ScorePhase;


public final class AbbeyAndMayorGame extends ExpandedGame {

    private Map<Player, Feature> returnedWagons = Maps.newHashMap();
    private Player wagonPlayer;
    private Set<Player> unusedAbbey = Sets.newHashSet();

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Wagon(game, player));
        player.addMeeple(new Mayor(game, player));
        player.addMeeple(new Barn(game, player));
        unusedAbbey.add(player);
    }

    public boolean hasUnusedAbbey(Player player) {
        return unusedAbbey.contains(player);
    }

    public void useAbbey(Player player) {
        if (! unusedAbbey.remove(player)) {
            throw new IllegalArgumentException("Player alredy used his abbey");
        }
    }

    @Override
    public void setGame(final Game game) {
        super.setGame(game);
        game.addGameListener(new GameEventAdapter() {
            @Override
            public void undeployed(Meeple m) {
                if (m instanceof Wagon && game.getPhase() instanceof ScorePhase) {
                    returnedWagons.put(m.getPlayer(), m.getFeature());
                }
            }
        });
    }

    public Map<Player, Feature> getReturnedWagons() {
        return returnedWagons;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("wagon-move");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            nl = ((Element)nl.item(0)).getElementsByTagName("neighbouring");
            for(int i = 0; i < nl.getLength(); i++) {
                processNeighbouringElement(tile, (Element) nl.item(i));
            }
        }
    }

    private void processNeighbouringElement(Tile tile, Element e) {
        String[] sides = asLocation(e);
        Feature[] te = new Feature[sides.length];
        for(int i = 0; i < te.length; i++) {
            te[i] = tile.getFeaturePartOf(Location.valueOf(sides[i]));
        }
        for(int i = 0; i < te.length; i++) {
            Feature[] neighbouring = new Feature[te.length-1];
            int ni = 0;
            for(int j = 0; j < te.length; j++) {
                if (j == i) continue;
                neighbouring[ni++] = te[j];
            }
            ((TileFeature)te[i]).addNeighbouring(neighbouring);
        }
    }

    @Override
    public void turnCleanUp() {
        returnedWagons.clear();
        wagonPlayer = null;
    }

    private Set<Location> copyWagonsLocations(Set<Location> locations) {
        Set<Location> result = Sets.newHashSet();
        for(Feature piece : getTile().getFeatures()) {
            Location loc = piece.getLocation();
            if (piece instanceof Road || piece instanceof City || piece instanceof Cloister) {
                if (locations.contains(loc)) {
                    result.add(loc);
                }
            }

        }
        return result;
    }

    private Set<Location> copyMayorLocations(Set<Location> locations) {
        Set<Location> result = Sets.newHashSet();
        for(Feature piece : getTile().getFeatures()) {
            Location loc = piece.getLocation();
            if (piece instanceof City && locations.contains(loc)) {
                result.add(loc);
            }

        }
        return result;
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
        Position pos = getTile().getPosition();
        Set<Location> tileLocations = commonSites.get(pos);
        if (game.getActivePlayer().hasFollower(Wagon.class)) {
            if (tileLocations != null) {
                Set<Location> wagonLocations = copyWagonsLocations(tileLocations);
                if (! wagonLocations.isEmpty()) {
                    actions.add(new MeepleAction(Wagon.class, pos, wagonLocations));
                }
            }
        }
        if (game.getActivePlayer().hasFollower(Mayor.class)) {
            if (tileLocations != null) {
                Set<Location> mayorLocations = copyMayorLocations(tileLocations);
                if (! mayorLocations.isEmpty()) {
                    actions.add(new MeepleAction(Mayor.class, pos, mayorLocations));
                }
            }
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        Position pos = getTile().getPosition();

        prepareFollowerActions(actions, commonSites);

        if (game.getActivePlayer().hasSpecialMeeple(Barn.class)) {
            BarnAction barnAction = null;
            Location corner = Location.WR.union(Location.NL);
            Location positionChange = Location.W;
            for (int i = 0; i < 4; i++) {
                if (isBarnCorner(corner, positionChange)) {
                    if (barnAction == null) {
                        barnAction = new BarnAction();
                        actions.add(barnAction);
                    }
                    barnAction.getOrCreate(pos).add(corner);
                }
                corner = corner.next();
                positionChange = positionChange.next();
            }
        }
    }

    private boolean isBarnCorner(Location corner, Location positionChange) {
        Farm farm = null;
        Position pos = getTile().getPosition();
        for(int i = 0; i < 4; i++) {
            Tile tile = getBoard().get(pos);
            if (tile == null) return false;
            farm = (Farm) tile.getFeaturePartOf(corner);
            if (farm == null) return false;
            corner = corner.next();
            pos = pos.add(positionChange);
            positionChange = positionChange.next();
        }

        if (! getGame().hasRule(CustomRule.MULTI_BARN_ALLOWED)) {
            return !farm.walk(new IsOccupied().with(Barn.class));
        }

        return true;
    }


    @Override
    public AbbeyAndMayorGame copy() {
        AbbeyAndMayorGame copy = new AbbeyAndMayorGame();
        copy.game = game;
        copy.unusedAbbey = Sets.newHashSet(unusedAbbey);
        copy.returnedWagons = Maps.newHashMap(returnedWagons);
        copy.wagonPlayer = wagonPlayer;
        return copy;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node, Expansion nodeFor) {
        for(Entry<Player, Feature> rv : returnedWagons.entrySet()) {
            Element el = doc.createElement("wagon");
            el.setAttribute("player", "" + rv.getKey().getIndex());
            el.setAttribute("loc", "" + rv.getValue().getLocation());
            XmlUtils.injectPosition(el, rv.getValue().getTile().getPosition());
            node.appendChild(el);
        }
        for(Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("abbey", "" + unusedAbbey.contains(player));
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("wagon");
        for(int i = 0; i < nl.getLength(); i++) {
            Element wg = (Element) nl.item(i);
            Location loc = Location.valueOf(wg.getAttribute("loc"));
            Position pos = XmlUtils.extractPosition(wg);
            int playerIndex = Integer.parseInt(wg.getAttribute("player"));
            Player player = game.getPlayer(playerIndex);
            returnedWagons.put(player, getBoard().get(pos).getFeature(loc));
        }
        nl = node.getElementsByTagName("player");
        for(int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            if (! Boolean.parseBoolean(playerEl.getAttribute("abbey"))) {
                useAbbey(player);
            }
        }
    }

    public Player getWagonPlayer() {
        return wagonPlayer;
    }

    public void setWagonPlayer(Player wagonPlayer) {
        this.wagonPlayer = wagonPlayer;
    }
}