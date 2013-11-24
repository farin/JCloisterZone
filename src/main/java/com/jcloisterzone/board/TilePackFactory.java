package com.jcloisterzone.board;

import static com.jcloisterzone.XmlUtils.attributeIntValue;
import static com.jcloisterzone.XmlUtils.attributeStringValue;
import static com.jcloisterzone.XmlUtils.getTileId;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.TunnelCapability;


public class TilePackFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileFactory tileFactory = new TileFactory();

    protected Game game;
    protected Ini config;
    protected Map<Expansion, Element> defs;

    private Set<String> usedIds = new HashSet<>(); //for assertion only


    public void setGame(Game game) {
        this.game = game;
        tileFactory.setGame(game);
    }

    public void setConfig(Ini config) {
        this.config = config;
    }

    public void setExpansions(Set<Expansion> expansions) {
        defs = Maps.newLinkedHashMap();
        for (Expansion expansion : expansions) {
            defs.put(expansion, getExpansionDefinition(expansion));
        }
    }

    public int getExpansionSize(Expansion expansion) {
        Element el = getExpansionDefinition(expansion);
        NodeList nl = el.getElementsByTagName("tile");
        int size = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Element tileElement = (Element) nl.item(i);
            String tileId = getTileId(expansion, tileElement);
            if (!Tile.ABBEY_TILE_ID.equals(tileId)) {
                size += getTileCount(tileElement, tileId);
            }
        }
        return size;
    }

    private URL getCardsConfig(Expansion expansion) {
        String fileName = config.get("debug", "tiles_"+expansion.name());
        if (fileName == null) {
            fileName = "tile-definitions/"+expansion.name().toLowerCase()+".xml";
        }
        return TilePackFactory.class.getClassLoader().getResource(fileName);
    }

    protected Element getExpansionDefinition(Expansion expansion) {
        return XmlUtils.parseDocument(getCardsConfig(expansion)).getDocumentElement();
    }

    protected Map<String, Integer> getDiscardTiles() {
        Map<String, Integer> discard = new HashMap<>();
        for (Element expansionDef: defs.values()) {
            NodeList nl = expansionDef.getElementsByTagName("discard");
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String tileId = el.getAttribute("tile");
                if (discard.containsKey(tileId)) {
                    discard.put(tileId, 1 + discard.get(tileId));
                } else {
                    discard.put(tileId, 1);
                }
            }
        }
        return discard;
    }

    protected boolean isTunnelActive(Expansion expansion) {
        return expansion == Expansion.TUNNEL ||
            (game.hasCapability(TunnelCapability.class) && game.hasRule(CustomRule.TUNNELIZE_ALL_EXPANSIONS));
    }

    protected int getTileCount(Element card, String tileId) {
        if (Tile.ABBEY_TILE_ID.equals(tileId)) {
            return PlayerSlot.COUNT;
        } else {
            return attributeIntValue(card, "count", 1);
        }
    }

    protected String getTileGroup(Tile tile, Element card) {
        String group = game.getTileGroup(tile);
        if (group != null) return group;
        return attributeStringValue(card, "group", DEFAULT_TILE_GROUP);
    }

    public List<Tile> createTiles(Expansion expansion, String tileId, Element card, Map<String, Integer> discardList) {
        if (usedIds.contains(tileId)) {
            throw new IllegalArgumentException("Multiple occurences of id " + tileId + " in tile definition xml.");
        }
        usedIds.add(tileId);

        int count = getTileCount(card, tileId);

        if (discardList.containsKey(tileId)) {
            int n = discardList.get(tileId);
            count -= n;
            if (count <= 0) { //discard can be in multiple expansions and than can be nagative
                return Collections.emptyList();
            }
        }

        List<Tile> tiles = new ArrayList<Tile>(count);
        for (int j = 0; j < count; j++) {
            Tile tile = tileFactory.createTile(expansion, tileId, card, isTunnelActive(expansion));
            game.initTile(tile, card); //must be called before rotation!
            tiles.add(tile);
        }
        return tiles;
    }

    public LinkedList<Position> getPreplacedPositions(String tileId, Element card) {
        NodeList nl = card.getElementsByTagName("position");
        if (nl.getLength() == 0) return null;

        LinkedList<Position> result = new LinkedList<Position>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element posEl = (Element) nl.item(i);
            result.add(new Position(attributeIntValue(posEl, "x"), attributeIntValue(posEl, "y")));
        }
        return result;
    }

    public DefaultTilePack createTilePack() {
        DefaultTilePack tilePack = new DefaultTilePack();

        Map<String, Integer> discardList = getDiscardTiles();

        for (Entry<Expansion, Element> entry: defs.entrySet()) {
            Expansion expansion = entry.getKey();
            NodeList nl = entry.getValue().getElementsByTagName("tile");
            for (int i = 0; i < nl.getLength(); i++) {
                Element tileElement = (Element) nl.item(i);
                String tileId = getTileId(expansion, tileElement);
                LinkedList<Position> positions = getPreplacedPositions(tileId, tileElement);
                for (Tile tile : createTiles(expansion, tileId, tileElement, discardList)) {
                    tilePack.addTile(tile, getTileGroup(tile, tileElement));
                    if (positions != null && !positions.isEmpty()) {
                        Position pos = positions.removeFirst();
                        //hard coded exceptions - should be declared in pack def
                        if (game.hasExpansion(Expansion.COUNT)) {
                            if (tile.getId().equals("BA.RCr")) continue;
                            if (tile.getId().equals("R1.I.s") ||
                                tile.getId().equals("R2.I.s") ||
                                tile.getId().equals("GQ.RFI")) {
                                pos = new Position(1, 2);
                            }
                            if (tile.getId().equals("WR.CFR")) {
                                pos = new Position(-2, -2);
                            }
                        } else if (game.hasExpansion(Expansion.WIND_ROSE)) {
                            if (tile.getId().equals("BA.RCr")) continue;
                            if (game.hasCapability(RiverCapability.class)) {
                                if (tile.getId().equals("WR.CFR")) {
                                    pos = new Position(0, 1);
                                }
                            }
                        }
                        logger.info("Setting initial placement {} for {}", pos, tile);
                        tile.setPosition(pos);
                    }
                }
            }
        }
        return tilePack;
    }

    public Game getGame() {
        return game;
    }

}
