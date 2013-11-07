package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CastleCapability extends Capability {

    private final Map<Player, Integer> castles = new HashMap<>();

    private Player castlePlayer;
    private Map<Player, Set<Location>> currentTileCastleBases = null;

    /** castles deployed this turn - cannot be scored - refs to master feature  */
    private final List<Castle> newCastles = new ArrayList<>();
    /** empty castles, already scored, keeping ref for game save */
    private final List<Castle> emptyCastles = new ArrayList<>();
    /** castles from previous turns, can be scored - castle -> vinicity area */
    private final Map<Castle, Position[]> scoreableCastleVicinity = new HashMap<>();
    private final Map<Castle, Integer> castleScore = new HashMap<>();

    public CastleCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restore(Object data) {
        throw new UnsupportedOperationException();
    }

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



    @Override
    public void initPlayer(Player player) {
        int players = game.getAllPlayers().length;
        if (players < 5) {
            castles.put(player, 3);
        } else {
            castles.put(player, 2);
        }
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City) {
            ((City) feature).setCastleBase(attributeBoolValue(xml, "castle-base"));
        }
    }

    private void checkCastleVicinity(Iterable<Position> triggerPositions, int score) {
        for (Position p : triggerPositions) {
            for (Entry<Castle, Position[]> entry : scoreableCastleVicinity.entrySet()) {
                Position[] vicinity = entry.getValue();
                for (int i = 0; i < vicinity.length; i++) {
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
        List<Meeple> meeples = city.getMeeples();
        for (Meeple m : meeples) {
            m.undeploy(false);
        }
        Castle castle = new Castle();
        castle.setTile(tile);
        castle.setId(game.idSequnceNextVal());
        castle.setLocation(loc.rotateCCW(tile.getRotation()));
        iter.set(castle);

        for (Feature f : tile.getFeatures()) { //replace also city references
            if (f instanceof Farm) {
                Farm farm = (Farm) f;
                Feature[] adjoining = farm.getAdjoiningCities();
                if (adjoining != null) {
                    for (int i = 0; i < adjoining.length; i++) {
                        if (adjoining[i] == city) {
                            adjoining[i] = castle;
                            break;
                        }
                    }
                }
            }
        }

        for (Meeple m : meeples) {
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
        for (Castle castle: newCastles) {
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


    public int getPlayerCastles(Player pl) {
        return castles.get(pl);
    }


    public void decreaseCastles(Player player) {
        int n = getPlayerCastles(player);
        if (n == 0) throw new IllegalStateException("Player has no castles");
        castles.put(player, n-1);
    }



    private Element createCastleXmlElement(Document doc, Castle castle) {
        Element el = doc.createElement("castle");
        el.setAttribute("location", castle.getLocation().toString());
        XmlUtils.injectPosition(el, castle.getTile().getPosition());
        return el;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("castles", "" + getPlayerCastles(player));
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
    }



    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            castles.put(player, Integer.parseInt(playerEl.getAttribute("castles")));
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
    }

}
