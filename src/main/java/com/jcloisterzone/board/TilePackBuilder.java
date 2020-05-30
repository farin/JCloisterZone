package com.jcloisterzone.board;

import static com.jcloisterzone.XMLUtils.attributeIntValue;
import static com.jcloisterzone.XMLUtils.getTileId;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


public class TilePackBuilder {

    public static class Tiles {
        private final TilePack tilePack;
        private Seq<PlacedTile> preplacedTiles;

        public Tiles(TilePack tilePack, Seq<PlacedTile> preplacedTiles) {
            super();
            this.tilePack = tilePack;
            this.preplacedTiles = preplacedTiles;
        }

        public TilePack getTilePack() {
            return tilePack;
        }

        public Seq<PlacedTile> getPreplacedTiles() {
            return preplacedTiles;
        }
    }

    public class Preplaced {
        final Position position;
        final int priority;

        public Preplaced(Position position, int priority) {
            this.position = position;
            this.priority = priority;
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileBuilder tileBuilder = new TileBuilder();

    protected GameState state;
    protected Map<Expansion, Integer> expansions;
    protected Config config;

    private java.util.Set<String> usedIds = new java.util.HashSet<>(); //for assertion only

    private java.util.Map<Expansion, Element> parsedDefinitions = new java.util.HashMap<>();
    private java.util.Map<String, java.util.List<Tile>> tiles = new java.util.HashMap<>();
    private Map<Position, Tuple2<PlacedTile, Integer>> preplacedTiles = HashMap.empty();

    public static class TileCount {
        public String tileId;
        public Integer count;

        public TileCount(String tileId, Integer count) {
            this.tileId = tileId;
            this.count = count;
        }
    }


    public void setGameState(GameState state) {
        this.state = state;
        tileBuilder.setGameState(state);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setExpansions(Map<Expansion, Integer> expansions) {
        this.expansions = expansions;
    }

    public Stream<TileCount> getExpansionTiles(Expansion expansion) {
        Element el = getExpansionDefinition(expansion);
        return XMLUtils.elementStream(el.getElementsByTagName("tile")).map(tileElement -> {
            String tileId = getTileId(expansion, tileElement);
            return new TileCount(tileId, getTileCount(tileElement, tileId, 1));
        });
    }

    public int getExpansionSize(Expansion expansion) {
        Element el = getExpansionDefinition(expansion);
        NodeList nl = el.getElementsByTagName("tile");
        int size = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Element tileElement = (Element) nl.item(i);
            String tileId = getTileId(expansion, tileElement);
            size += getTileCount(tileElement, tileId, 1);
        }
        return size;
    }

    protected  URL getStandardTilesConfig(Expansion expansion) {
        String fileName = "tile-definitions/"+expansion.name().toLowerCase()+".xml";
        return TilePackBuilder.class.getClassLoader().getResource(fileName);
    }

    protected URL getTilesConfig(Expansion expansion) {
        DebugConfig debugConfig = config.getDebug();
        String fileName = null;
        if (debugConfig != null && debugConfig.getTile_definitions() != null) {
            fileName = debugConfig.getTile_definitions().get(expansion.name());
        }
        if (fileName == null) {
            fileName = "tile-definitions/"+expansion.name().toLowerCase()+".xml";
            if (expansion.getOrigin() != null) {
                // expansion is created by plugin
                return expansion.getOrigin().getLoader().getResource(fileName);
            }
        }
        if (fileName.startsWith("/")) {
            try {
                return new File(fileName).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return TilePackBuilder.class.getClassLoader().getResource(fileName);
        }
    }

    protected Element getExpansionDefinition(Expansion expansion) {
        Element root = parsedDefinitions.get(expansion);
        if (root == null) {
            try {
                root = XMLUtils.parseDocument(getTilesConfig(expansion)).getDocumentElement();
                parsedDefinitions.put(expansion, root);
            } catch (Exception e) {
                logger.error("Can't parse definition for " + expansion.name(), e);
            }
        }
        return root;
    }

    protected boolean isTunnelActive(Expansion expansion) {
        if (!state.getCapabilities().contains(TunnelCapability.class)) {
            return false;
        }
        return expansion == Expansion.TUNNEL || state.getBooleanValue(Rule.TUNNELIZE_ALL_EXPANSIONS);
    }

    protected int getTileCount(Element tileEl, String tileId, int expansionCount) {
        int baseCount = attributeIntValue(tileEl, "count", 1);
        return Math.min(expansionCount * baseCount, attributeIntValue(tileEl, "maxCount", Integer.MAX_VALUE));
    }

    protected String getTileGroup(Tile tile, Vector<Element> tileElements) {
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            String group = cap.getTileGroup(tile);
            if (group != null) return group;
        }
        for (Element tileElement : tileElements) {
            String group = tileElement.getAttribute("group");
            if (!group.isEmpty()) {
                return group;
            }
        }
        return DEFAULT_TILE_GROUP;
    }

    public Tile initTile(Tile tile, Vector<Element> tileElements) throws RemoveTileException {
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            tile = cap.initTile(state, tile, tileElements);
        }
        return tile;
    }

    public Tile createTile(Expansion expansion, String tileId, Vector<Element> tileElements) throws RemoveTileException {
        if (usedIds.contains(tileId)) {
            throw new IllegalArgumentException("Multiple occurences of id " + tileId + " in tile definition xml.");
        }
        usedIds.add(tileId);

        Tile tile = tileBuilder.createTile(expansion, tileId, tileElements, isTunnelActive(expansion));
        return initTile(tile, tileElements);
    }

    public Stream<Preplaced> getPreplacedPositions(String tileId, Vector<Element> tileElements) {
        return Stream.concat(
            tileElements.map(el -> XMLUtils.elementStream(el.getElementsByTagName("position")))
        ).map(
            e -> {
                Position pos = new Position(attributeIntValue(e, "x"), attributeIntValue(e, "y"));
                return new Preplaced(pos, attributeIntValue(e, "priority", 1));
            }
        );
    }

    public Element findTileElement(String id) {
        String[] tokens = id.split("\\.", 2);
        Expansion expansion = Expansion.valueOfCode(tokens[0]);
        Element element = getExpansionDefinition(expansion);
        NodeList nl = element.getElementsByTagName("tile");
        for (int i = 0; i < nl.getLength(); i++) {
            Element tileElement =  (Element) nl.item(i);
            if (tileElement.getAttribute("id").equals(tokens[1])) {
                return tileElement;
            }
        }
        throw new NoSuchElementException();
    }

    @SuppressWarnings("unchecked")
    public Tiles createTilePack() {
        expansions.forEach(t -> {
            Expansion expansion = t._1;
            Element element = getExpansionDefinition(expansion);
            int expansionCount = Math.min(t._2, attributeIntValue(element, "maxCount", 5));

            NodeList nl = element.getElementsByTagName("tile");
            XMLUtils.elementStream(nl).forEach(tileElement -> {
                String capabilityClass = tileElement.getAttribute("if-capability");
                if (!capabilityClass.isEmpty()) {
                    try {
                        Class<? extends Capability<?>> cls = (Class<? extends Capability<?>>) Class.forName(capabilityClass);
                        if (!state.getCapabilities().contains(cls)) {
                            return;
                        }
                    } catch (ClassNotFoundException e) {
                        logger.error("Can't find " + capabilityClass, e);
                    }
                }


                String extendsTile = tileElement.getAttribute("extends");
                Vector<Element> tileElements = Vector.of(tileElement);
                if (!extendsTile.isEmpty()) {
                    Element parentElement = findTileElement(extendsTile);
                    tileElements = tileElements.append(parentElement);
                }

                String tileId = getTileId(expansion, tileElement);
                List<Preplaced> positions = getPreplacedPositions(tileId, tileElements).toList();
                int count = getTileCount(tileElement, tileId, expansionCount);

                Tile tile;
                try {
                    tile = createTile(expansion, tileId, tileElements);
                } catch (RemoveTileException ex) {
                    return;
                }

                for (int ci = 0; ci < count; ci++) {
                    Position pos = null;
                    int priority = 0;
                    if (positions != null && !positions.isEmpty()) {
                        Preplaced pp = positions.peek();
                        pos = pp.position;
                        priority = pp.priority;
                        positions = positions.pop();
                        //hard coded exceptions - should be declared in pack def
                        // TODO add <remap> ... directive
                        if (expansions.containsKey(Expansion.COUNT)) {
                            if (tileId.equals("BA.RCr")) continue;
                            if (tileId.equals("R1.I.s") ||
                                tileId.equals("R2.I.s") ||
                                tileId.equals("GQ.RFI")) {
                                pos = new Position(1, 2);
                            }
                            if (tileId.equals("WR.CFR")) {
                                pos = new Position(-2, -2);
                            }
                        } else if (expansions.containsKey(Expansion.WIND_ROSE)) {
                            if (state.getCapabilities().contains(RiverCapability.class)) {
                                if (tileId.equals("WR.CFR")) {
                                    pos = new Position(0, 1);
                                }
                            }
                        }
                        logger.info("Setting initial placement {} for {}", pos, tileId);
                    }
                    if (pos != null) {
                        Tuple2<PlacedTile, Integer> pt = preplacedTiles.get(pos).getOrNull();
                        if (pt == null || pt._2 < priority) {
                            preplacedTiles = preplacedTiles.put(pos,
                                new Tuple2<>(new PlacedTile(tile, pos, Rotation.R0), priority)
                            );
                        }
                    } else {
                        String group = getTileGroup(tile, tileElements);
                        if (!tiles.containsKey(group)) {
                            tiles.put(group, new java.util.ArrayList<>());
                        }
                        tiles.get(group).add(tile);
                    }
                }
            });
        });


        /* sort groups and tiles to get deterministic item order
         * This required for stable behavior when game is loaded with same seed, tiles must return same tile for same index
         */
        LinkedHashMap<String, TileGroup> groups = LinkedHashMap.empty();
        Vector<String> groupNames = Vector.ofAll(tiles.keySet()).sorted();
        for (String name : groupNames) {
            java.util.List<Tile> groupTiles = tiles.get(name);
            groups = groups.put(name, new TileGroup(name, Vector.ofAll(groupTiles).sortBy(Tile::getId), true));
        }

        return new Tiles(
            new TilePack(groups, 0),
            preplacedTiles.values().map(Tuple2::_1)
        );
    }
}
