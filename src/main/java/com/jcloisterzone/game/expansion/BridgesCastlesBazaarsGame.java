package com.jcloisterzone.game.expansion;

import static com.jcloisterzone.board.XmlUtils.attributeBoolValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.Game;

public class BridgesCastlesBazaarsGame extends ExpandedGame {

    private boolean bridgeUsed;
    private Map<Player, Integer> bridges = Maps.newHashMap();
    private Map<Player, Integer> castles = Maps.newHashMap();

    private Player castlePlayer;
    private Map<Player, Set<Location>> currentTileCastleBases = null;

    /** castles deployed this turn - cannot be scored - refs to master feature  */
    private List<Castle> newCastles = Lists.newArrayList();
    /** empty castles, already scored, keeping ref for game save */
    private List<Castle> emptyCastles = Lists.newArrayList();
    /** castles from previous turns, can be scored - castle -> vinicity area */
    private Map<Castle, Position[]> scoreableCastleVicinity = Maps.newHashMap();
    private Map<Castle, Integer> castleScore = Maps.newHashMap();

    private ArrayList<BazaarItem> bazaarSupply;
    private BazaarItem currentBazaarAuction;
    private Player bazaarTileSelectingPlayer;
    private Player bazaarBiddingPlayer;

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        game.addGameListener(new GameEventAdapter() {
            @Override
            public void castleDeployed(Castle castle1, Castle castle2) {
                newCastles.add(castle1.getMaster());
            }

            @Override
            public void undeployed(Meeple meeple) {
                if (meeple.getFeature() instanceof Castle) {
                    Castle castle = (Castle) meeple.getFeature().getMaster();
                    scoreableCastleVicinity.remove(castle);
                    emptyCastles.add(castle);
                }
            }
        });
    }

    @Override
    public void initPlayer(Player player) {
        int players = game.getAllPlayers().length;
        if (players < 5) {
            bridges.put(player, 3);
            castles.put(player, 3);
        } else {
            bridges.put(player, 2);
            castles.put(player, 2);
        }
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("bazaar").getLength() > 0) {
            tile.setTrigger(TileTrigger.BAZAAR);
        }
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City) {
            ((City) feature).setCastleBase(attributeBoolValue(xml, "castle-base"));
        }
    }

    private void checkCastleVicinity(Iterable<Position> triggerPositions, int score) {
        for(Position p : triggerPositions) {
            for(Entry<Castle, Position[]> entry : scoreableCastleVicinity.entrySet()) {
                Position[] vicinity = entry.getValue();
                for(int i = 0; i < vicinity.length; i++) {
                    if (vicinity[i].equals(p)) {
                        Castle master = entry.getKey();
                        Integer currentCastleScore = castleScore.get(master);
                        if (currentCastleScore == null || currentCastleScore < score) {
                            castleScore.put(master, score);
                            //chain reaction, one completed castle triggers another
                            checkCastleVicinity(Arrays.asList(master.getCastleBase()), score);
                        }
                        break;
                    }
                }
            }
        }
    }

    private Castle replaceCityWithCastle(Tile tile, Location loc) {
        ListIterator<Feature> iter = tile.getFeatures().listIterator();
        City city = null;
        while(iter.hasNext()) {
            Feature feature =  iter.next();
            if (feature.getLocation() == loc) {
                city = (City) feature;
                break;
            }
        }
        Meeple m = city.getMeeple();
        if (m != null) {
            m.undeploy(false);
        }
        Castle castle = new Castle();
        castle.setTile(tile);
        castle.setId(game.idSequnceNextVal());
        castle.setLocation(loc.rotateCCW(tile.getRotation()));
        iter.set(castle);

        for(Feature f : tile.getFeatures()) { //replace also city references
            if (f instanceof Farm) {
                Farm farm = (Farm) f;
                Feature[] adjoining = farm.getAdjoiningCities();
                if (adjoining != null) {
                    for(int i = 0; i < adjoining.length; i++) {
                        if (adjoining[i] == city) {
                            adjoining[i] = castle;
                            break;
                        }
                    }
                }
            }
        }

        if (m != null) {
            m.deploy(tile, loc);
        }
        return castle;
    }

    public Castle convertCityToCastle(Position pos, Location loc) {
        Castle castle1 = replaceCityWithCastle(getBoard().get(pos), loc);
        Castle castle2 = replaceCityWithCastle(getBoard().get(pos.add(loc)), loc.rev());
        castle1.getEdges()[0] = castle2;
        castle2.getEdges()[0] = castle1;
        game.fireGameEvent().castleDeployed(castle1, castle2);
        return castle1.getMaster();
    }

    @Override
    public void scoreCompleted(CompletableScoreContext ctx) {
        checkCastleVicinity(ctx.getPositions(), ctx.getPoints());
    }

    public Map<Castle, Integer> getCastleScore() {
        return castleScore;
    }

    @Override
    public void turnCleanUp() {
        bridgeUsed = false;
        for(Castle castle: newCastles) {
            scoreableCastleVicinity.put(castle, castle.getVicinity());
        }
        newCastles.clear();
        castleScore.clear();
    }

    public Player getCastlePlayer() {
        return castlePlayer;
    }

    public void setCastlePlayer(Player castlePlayer) {
        this.castlePlayer = castlePlayer;
    }

    public Map<Player, Set<Location>> getCurrentTileCastleBases() {
        return currentTileCastleBases;
    }

    public void setCurrentTileCastleBases(Map<Player, Set<Location>> currentTileCastleBases) {
        this.currentTileCastleBases = currentTileCastleBases;
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        if (! bridgeUsed && getPlayerBridges(game.getPhase().getActivePlayer()) > 0) {
            BridgeAction action = prepareBridgeAction();
            if (action != null) {
                actions.add(action);
            }
        }
    }

    public BridgeAction prepareMandatoryBridgeAction() {
        Tile tile = game.getCurrentTile();
        for(Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
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
        for(Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            Tile adjacent = entry.getValue();
            Location rel = entry.getKey();
            action = prepareTileBridgeAction(adjacent, action, getBridgeLocationForAdjacent(rel));
        }
        return action;
    }

    private BridgeAction prepareTileBridgeAction(Tile tile, BridgeAction action, Location bridgeLoc) {
        if (isBridgePlacementAllowed(tile, tile.getPosition(), bridgeLoc)) {
            if (action == null) action = new BridgeAction();
            action.getSites().getOrCreate(tile.getPosition()).add(bridgeLoc);
        }
        return action;
    }

    private boolean isBridgePlacementAllowed(Tile tile, Position p, Location bridgeLoc) {
        if (! tile.isBridgeAllowed(bridgeLoc)) return false;
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

    @Override
    public boolean isSpecialPlacementAllowed(Tile tile, Position p) {
        if (getPlayerBridges(game.getActivePlayer()) > 0) {
            if (isTilePlacementWithBridgeAllowed(tile, p, Location.NS)) return true;
            if (isTilePlacementWithBridgeAllowed(tile, p, Location.WE)) return true;
            if (isTilePlacementWithOneAdjacentBridgeAllowed(tile, p)) return true;
        }
        return false;
    }

    private boolean isTilePlacementWithBridgeAllowed(Tile tile, Position p, Location bridgeLoc) {
        if (! tile.isBridgeAllowed(bridgeLoc)) return false;

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
                if (! isBridgePlacementAllowed(adjacent, adjacent.getPosition(), bridgeLoc)) return false;
                bridgeUsed = true;
            }
        }
        return bridgeUsed; //ok if exactly one bridge is used
    }

    public int getPlayerCastles(Player pl) {
        return castles.get(pl);
    }

    public int getPlayerBridges(Player pl) {
        return bridges.get(pl);
    }

    public void decreaseCastles(Player player) {
        int n = getPlayerCastles(player);
        if (n == 0) throw new IllegalStateException("Player has no castles");
        castles.put(player, n-1);
    }

    public void decreaseBridges(Player player) {
        int n = getPlayerBridges(player);
        if (n == 0) throw new IllegalStateException("Player has no bridges");
        bridges.put(player, n-1);
    }

    public void deployBridge(Position pos, Location loc) {
        Tile tile = getBoard().get(pos);
        if (! tile.isBridgeAllowed(loc)) {
            throw new IllegalArgumentException("Cannot deploy " + loc + " bridge on " + pos);
        }
        bridgeUsed = true;
        tile.placeBridge(loc);
        game.fireGameEvent().bridgeDeployed(pos, loc);
    }

    @Override
    public BridgesCastlesBazaarsGame copy() {
        BridgesCastlesBazaarsGame copy = new BridgesCastlesBazaarsGame();
        copy.game = game;
        copy.bridges = Maps.newHashMap(bridges);
        copy.castles = Maps.newHashMap(castles);
        return copy;
    }

    private Element createCastleXmlElement(Document doc, Castle castle) {
        Element el = doc.createElement("castle");
        el.setAttribute("location", castle.getLocation().toString());
        XmlUtils.injectPosition(el, castle.getTile().getPosition());
        return el;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        node.setAttribute("bridgeUsed", bridgeUsed + "");
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("castles", "" + getPlayerCastles(player));
            el.setAttribute("bridges", "" + getPlayerBridges(player));
        }

        for (Castle castle : scoreableCastleVicinity.keySet()) {
            node.appendChild(createCastleXmlElement(doc, castle));
        }
        for (Castle castle : newCastles) {
            Element el = createCastleXmlElement(doc, castle);
            el.setAttribute("new", "true");
            node.appendChild(el);
        }
        for (Castle castle : emptyCastles) {
            Element el = createCastleXmlElement(doc, castle);
            el.setAttribute("completed", "true");
            node.appendChild(el);
        }

        if (bazaarSupply != null) {
            for (BazaarItem bi : bazaarSupply) {
                Element el = doc.createElement("bazaar-supply");
                el.setAttribute("tile", bi.getTile().getId());
                if (bi.getOwner() != null) el.setAttribute("owner", ""+bi.getOwner().getIndex());
                if (bi.getCurrentBidder() != null) el.setAttribute("bidder", ""+bi.getCurrentBidder().getIndex());
                el.setAttribute("price", ""+bi.getCurrentPrice());

                if (currentBazaarAuction == bi) {
                    el.setAttribute("selected", "true");
                }
                node.appendChild(el);
            }
        }
        if (bazaarTileSelectingPlayer != null) {
            Element el = doc.createElement("bazaar-selecting-player");
            el.setAttribute("player", ""+bazaarTileSelectingPlayer.getIndex());
            node.appendChild(el);
        }
        if (bazaarBiddingPlayer != null) {
            Element el = doc.createElement("bazaar-bidding-player");
            el.setAttribute("player", ""+bazaarBiddingPlayer.getIndex());
            node.appendChild(el);
        }

        //TODO save bridges - add method to enrich tile element by expansions
        // ??? move also fairy like elements ?
    }

    @Override
    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
        if (tile.getBridge() != null) {
            Location realLoc = tile.getBridge().getLocation().rotateCW(tile.getRotation());
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
    public void loadFromSnapshot(Document doc, Element node) {
        bridgeUsed = Boolean.parseBoolean(node.getAttribute("bridgeUsed"));
        NodeList nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            castles.put(player, Integer.parseInt(playerEl.getAttribute("castles")));
            bridges.put(player, Integer.parseInt(playerEl.getAttribute("bridges")));
        }

        nl = node.getElementsByTagName("castle");
        for (int i = 0; i < nl.getLength(); i++) {
            Element castleEl = (Element) nl.item(i);
            Position pos = XmlUtils.extractPosition(castleEl);
            Location loc = Location.valueOf(castleEl.getAttribute("location"));
            Castle castle = convertCityToCastle(pos, loc);
            boolean isNew = XmlUtils.attributeBoolValue(castleEl, "new");
            boolean isCompleted = XmlUtils.attributeBoolValue(castleEl, "completed");
            if (isNew) {
                newCastles.add(castle);
            } else if (isCompleted) {
                emptyCastles.add(castle);
            } else {
                scoreableCastleVicinity.put(castle, castle.getVicinity());
            }
        }

        nl = node.getElementsByTagName("bazaar-supply");
        if (nl.getLength() > 0) {
            bazaarSupply = new ArrayList<BazaarItem>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Tile tile = game.getTilePack().drawTile(el.getAttribute("tile"));
                BazaarItem bi = new BazaarItem(tile);
                bazaarSupply.add(bi);
                if (el.hasAttribute("owner")) bi.setOwner(game.getPlayer(Integer.parseInt(el.getAttribute("owner"))));
                if (el.hasAttribute("bidder")) bi.setCurrentBidder(game.getPlayer(Integer.parseInt(el.getAttribute("bidder"))));
                bi.setCurrentPrice(XmlUtils.attributeIntValue(el, "price"));
                if (XmlUtils.attributeBoolValue(el, "selected")) {
                    currentBazaarAuction = bi;
                }
            }
        }

        nl = node.getElementsByTagName("bazaar-selecting-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarTileSelectingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
        nl = node.getElementsByTagName("bazaar-bidding-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarBiddingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
    }

    public ArrayList<BazaarItem> getBazaarSupply() {
        return bazaarSupply;
    }

    public void setBazaarSupply(ArrayList<BazaarItem> bazaarSupply) {
        this.bazaarSupply = bazaarSupply;
    }

    public Player getBazaarTileSelectingPlayer() {
        return bazaarTileSelectingPlayer;
    }

    public void setBazaarTileSelectingPlayer(Player bazaarTileSelectingPlayer) {
        this.bazaarTileSelectingPlayer = bazaarTileSelectingPlayer;
    }

    public Player getBazaarBiddingPlayer() {
        return bazaarBiddingPlayer;
    }

    public void setBazaarBiddingPlayer(Player bazaarBiddingPlayer) {
        this.bazaarBiddingPlayer = bazaarBiddingPlayer;
    }

    public BazaarItem getCurrentBazaarAuction() {
        return currentBazaarAuction;
    }

    public void setCurrentBazaarAuction(BazaarItem currentBazaarAuction) {
        this.currentBazaarAuction = currentBazaarAuction;
    }

    public boolean hasTileAuctioned(Player p) {
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) return true;
        }
        return false;
    }

    public Tile drawNextTile() {
        if (bazaarSupply == null) return null;
        Player p = game.getActivePlayer();
        Tile tile = null;
        BazaarItem currentItem = null;
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) {
                currentItem = bi;
                tile = bi.getTile();
                break;
            }
        }
        bazaarSupply.remove(currentItem);
        if (bazaarSupply.isEmpty()) {
            bazaarSupply = null;
        }
        return tile;
    }

    public List<Tile> getDrawQueue() {
        if (bazaarSupply == null) return Collections.emptyList();
        List<Tile> result = Lists.newArrayList();
        Player turnPlayer = game.getTurnPlayer();
        Player p = game.getNextPlayer(turnPlayer);
        while (p != turnPlayer) {
            for(BazaarItem bi : bazaarSupply) {
                if (bi.getOwner() == p) {
                    result.add(bi.getTile());
                    break;
                }
            }
            p = game.getNextPlayer(p);
        }
        return result;
    }

}