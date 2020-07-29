package com.jcloisterzone.board;

import static com.jcloisterzone.XMLUtils.attributeIntValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;


public class TilePackBuilder {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileBuilder tileBuilder = new TileBuilder();

    protected GameState state;
    protected Map<String, Integer> tileSets;
    protected Config config;

    private java.util.Set<String> usedIds = new java.util.HashSet<>(); //for assertion only
    private java.util.Map<String, java.util.List<Tile>> tiles = new java.util.HashMap<>();

    public void setGameState(GameState state) {
        this.state = state;
        tileBuilder.setGameState(state);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setTileSets(Map<String, Integer> tileSets) {
        this.tileSets = tileSets;
    }

    protected  URL getStandardTilesConfig(Expansion expansion) {
        String fileName = "tile-definitions/"+expansion.name().toLowerCase()+".xml";
        return TilePackBuilder.class.getClassLoader().getResource(fileName);
    }


    protected boolean isTunnelActive(String tileId) {
        if (!state.getCapabilities().contains(TunnelCapability.class)) {
            return false;
        }
        return tileId.startsWith("TU/") || state.getBooleanRule(Rule.TUNNELIZE_OTHER_EXPANSIONS);
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

    public Tile createTile(String tileId, Vector<Element> tileElements) throws RemoveTileException {
        if (usedIds.contains(tileId)) {
            throw new IllegalArgumentException("Multiple occurences of id " + tileId + " in tile definition xml.");
        }
        usedIds.add(tileId);

        Tile tile = tileBuilder.createTile(tileId, tileElements, isTunnelActive(tileId));
        return initTile(tile, tileElements);
    }


    @SuppressWarnings("unchecked")
    public TilePack createTilePack() throws IOException {
        java.util.Map<String, Integer> tilesCount = new java.util.HashMap<>();
        Path definitionsDir = new File(TilePackBuilder.class.getClassLoader().getResource("tile-definitions").getFile()).toPath();
        //Path definitionsDir = new File("tile-definitions").toPath();

        Files.list(definitionsDir).forEach(path -> {
            Element element = XMLUtils.parseDocument(path).getDocumentElement();

            XMLUtils.elementStream(element.getElementsByTagName("tile-set")).forEach(tileSetElement -> {
                String tileSetId = tileSetElement.getAttribute("id");
                int setCount = tileSets.getOrElse(tileSetId, 0);
                if (setCount > 0) {
                    XMLUtils.elementStream(tileSetElement.getElementsByTagName("ref")).forEach(refElement -> {
                        String tileId = refElement.getAttribute("tile");
                        try {
                            int tileCount = Integer.parseInt(refElement.getAttribute("count"));
                            tilesCount.put(tileId, tilesCount.getOrDefault(tileId, 0) + setCount * tileCount);
                        } catch (Exception e) {
                            System.err.println("Can't parse " + tileId + ": " + e);
                        }
                    });
                }
            });
        });

        Files.list(definitionsDir).forEach(path -> {
            Element element = XMLUtils.parseDocument(path).getDocumentElement();

            NodeList nl = element.getElementsByTagName("tile");
            XMLUtils.elementStream(nl).forEach(tileElement -> {
                String tileId = tileElement.getAttribute("id");
                int count = tilesCount.getOrDefault(tileId, 0);
                if (count == 0) {
                    return;
                }

                // TODO extends
                //String extendsTile = tileElement.getAttribute("extends");
                Vector<Element> tileElements = Vector.of(tileElement);
//                if (!extendsTile.isEmpty()) {
//                    Element parentElement = findTileElement(extendsTile);
//                    tileElements = tileElements.append(parentElement);
//                }


//                List<Preplaced> positions = getPreplacedPositions(tileId, tileElements).toList();
//                int count = getTileCount(tileElement, tileId, expansionCount);

                Tile tile;
                try {
                    tile = createTile(tileId, tileElements);
                } catch (RemoveTileException ex) {
                    return;
                }

                String groupId = getTileGroup(tile, tileElements);
                java.util.List<Tile> group = tiles.get(groupId);
                if (group == null) {
                    group = new java.util.ArrayList<>();
                    tiles.put(groupId, group);
                }
                for (int ci = 0; ci < count; ci++) {
                    group.add(tile);
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

        return new TilePack(groups, 0);
    }
}
