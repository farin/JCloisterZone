package com.jcloisterzone.board;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.feature.modifier.BooleanAllModifier;
import com.jcloisterzone.feature.modifier.BooleanAnyModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.feature.modifier.IntegerAddModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.setup.SetupQuery;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static com.jcloisterzone.XMLUtils.attributeIntValue;


public class TilePackBuilder {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TILE_GROUP = "default";

    private final TileBuilder tileBuilder = new TileBuilder();

    protected GameState state;
    protected Map<String, Integer> tileSets;

    private java.util.Set<String> usedIds = new java.util.HashSet<>(); //for assertion only
    private java.util.Map<String, java.util.List<Tile>> tiles = new java.util.HashMap<>();

    public void setGameState(GameState state) {
        this.state = state;
        tileBuilder.setGameState(state);
    }

    public void setTileSets(Map<String, Integer> tileSets) {
        this.tileSets = tileSets;
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


    public TilePack createTilePack(java.util.List<String> definitions) throws IOException {
        java.util.Map<String, Integer> tilesCount = new java.util.HashMap<>();
        java.util.Set<String> removedTiles = new java.util.HashSet<>();
        java.util.List<FeatureModifier> modifiers = new java.util.ArrayList<>();

        definitions.forEach(path -> {
            InputStream defFile;
            try {
                defFile = new FileInputStream(new File(path));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            }
            Element element = XMLUtils.parseDocument(defFile).getDocumentElement();

            XMLUtils.elementStream(element.getElementsByTagName("modifier")).forEach(modifierEl -> {
                Class<? extends FeatureModifier> cls = null;
                switch (modifierEl.getAttribute("type")) {
                    case "+": cls = IntegerAddModifier.class; break;
                    case "all": cls = BooleanAllModifier.class; break;
                    case "any": cls = BooleanAnyModifier.class; break;
                }
                String selector = modifierEl.getAttribute("selector");
                SetupQuery enabledBy = null;
                if (modifierEl.hasAttribute("enabled-by")) {
                    enabledBy = new GameElementQuery(modifierEl.getAttribute("enabled-by"));
                }
                var scoringScript= modifierEl.getElementsByTagName("scoring").item(0).getTextContent();

                try {
                    FeatureModifier modifier = cls.getConstructor(String.class, SetupQuery.class).newInstance(selector, enabledBy);
                    modifier.setScoringScript(scoringScript);
                    modifiers.add(modifier);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            });

            XMLUtils.elementStream(element.getElementsByTagName("tile-set")).forEach(tileSetElement -> {
                String tileSetId = tileSetElement.getAttribute("id");
                int setCount = tileSets.getOrElse(tileSetId, 0);
                if (setCount > 0) {
                    XMLUtils.elementStream(tileSetElement.getElementsByTagName("ref")).forEach(refElement -> {
                        String tileId = refElement.getAttribute("tile");
                        try {
                            int tileCount = Integer.parseInt(refElement.getAttribute("count"));
                            int count = tilesCount.getOrDefault(tileId, 0) + setCount * tileCount;
                            tilesCount.put(tileId, count);
                        } catch (Exception e) {
                            System.err.println("Can't parse " + tileId + ": " + e);
                        }
                    });
                    XMLUtils.elementStream(tileSetElement.getElementsByTagName("remove")).forEach(removeElement -> {
                        removedTiles.add(removeElement.getAttribute("tile"));
                    });
                }
            });

            try {
                defFile.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        });

        tileBuilder.setExternalModifiers(modifiers);

        definitions.forEach(path -> {
            InputStream defFile;
            try {
                defFile = new FileInputStream(new File(path));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            }
            Element element = XMLUtils.parseDocument(defFile).getDocumentElement();

            NodeList nl = element.getElementsByTagName("tile");
            XMLUtils.elementStream(nl).forEach(tileElement -> {
                String tileId = tileElement.getAttribute("id");
                int count = tilesCount.getOrDefault(tileId, 0);
                if (count == 0 || removedTiles.contains(tileId)) {
                    return;
                }

                if (tileElement.hasAttribute("max")) {
                    count = Math.min(count, Integer.parseInt(tileElement.getAttribute("max")));
                }

                Vector<Element> tileElements = Vector.of(tileElement);
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

            try {
                defFile.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
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
