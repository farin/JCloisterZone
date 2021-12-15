package com.jcloisterzone.board;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.WatchtowerCapability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import static com.jcloisterzone.XMLUtils.*;


public class TileBuilder {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final FeatureModifier[] MONASTERY_MODIFIERS = new FeatureModifier[] { Monastery.SPECIAL_MONASTERY, Monastery.SHRINE, Monastery.CHURCH };
    private static final FeatureModifier[] CITY_MODIFIERS = new FeatureModifier[] { City.PENNANTS, City.CATHEDRAL, City.PRINCESS, City.BESIEGED, City.DARMSTADTIUM, City.POINTS_MODIFIER };
    private static final FeatureModifier[] ROAD_MODIFIERS = new FeatureModifier[] { Road.INN, Road.LABYRINTH };

    private java.util.List<FeatureModifier> externalModifiers;
    private java.util.Map<String, java.util.List<FeatureModifier>> modifiersByType;

    private java.util.Map<FeaturePointer, Feature> features;
    private java.util.List<Tuple3<ShortEdge, Location, FeaturePointer>> multiEdges; //Edge, edge location, target feature (which is declared without edge)
    private java.util.Map<String, java.util.List<FeaturePointer>> neighbouring;
    private String tileId;

    private GameState state;

    public GameState getGameState() {
        return state;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    public void setExternalModifiers(java.util.List<FeatureModifier> externalModifiers) {
        this.externalModifiers = externalModifiers;
        this.modifiersByType = new java.util.HashMap<>();
        modifiersByType.put("road", new ArrayList<>(Arrays.asList(ROAD_MODIFIERS)));
        modifiersByType.put("city", new ArrayList<>(Arrays.asList(CITY_MODIFIERS)));
        modifiersByType.put("monastery", new ArrayList<>(Arrays.asList(MONASTERY_MODIFIERS)));
        for (FeatureModifier mod : externalModifiers) {
            String key = mod.getSelector().split("\\[")[0];
            var list = modifiersByType.get(key);
            if (list == null) {
                list = new ArrayList<>();
                modifiersByType.put(key, list);
            }
            list.add(mod);
        }
    }

    public java.util.List<FeatureModifier> getExternalModifiers() {
        return externalModifiers;
    }

    public Tile createTile(String tileId, Element tileElement, boolean isTunnelActive) {

        features = new java.util.HashMap<>();
        multiEdges = new java.util.ArrayList<>();
        neighbouring = new java.util.HashMap<>();
        Set<TileModifier> tileModifiers = HashSet.empty();
        this.tileId = tileId;

        logger.debug("Creating " + tileId);


        NodeList nl = tileElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (!(nl.item(i) instanceof Element)) continue;
            Element el = (Element) nl.item(i);
            switch (el.getTagName()) {
                case "monastery":
                    processMonasteryElement(el);
                    break;
                case "road":
                    processRoadElement(el, isTunnelActive);
                    break;
                case "city":
                    processCityElement(el);
                    break;
                case "field":
                    processFieldElement(el);
                    break;
                case "river":
                    processRiverElement(el);
                    break;
                case "tower":
                    initFeature(el, new Tower());
                    break;
                case "yaga-hut":
                    initFeature(el, new YagaHut());
                    break;
                case "watchtower":
                    tileModifiers = tileModifiers.add(new WatchtowerCapability.WatchtowerModifier(el.getAttribute("bonus")));
                    break;
                case "circus":
                    // init feature even if capability is not enabled, because of ringmaster scoring
                    initFeature(el, new Circus());
                    break;
                case "acrobats":
                    // init feature even if capability is not enabled, because of ringmaster scoring
                    initFeature(el, new Acrobats());
                    break;
            }
        }

        for (Tuple3<ShortEdge, Location, FeaturePointer> multiEdge: multiEdges) {
        	java.util.Map.Entry<FeaturePointer, Feature> matched = null;
        	for (java.util.Map.Entry<FeaturePointer, Feature> entry : features.entrySet()) {
        		if (multiEdge._2.isPartOf(entry.getKey().getLocation())) {
        			matched = entry;
        			break;
        		}
        	}

        	if (matched == null) {
        		throw new IllegalArgumentException("Matching city not found");
        	}
        	City target = (City) matched.getValue();
        	assert target.getOpenEdges().contains(multiEdge._1.toEdge());
        	Set<Tuple2<ShortEdge, FeaturePointer>> targetMultiEdges = target.getMultiEdges();
        	targetMultiEdges = targetMultiEdges.add(new Tuple2<>(multiEdge._1, multiEdge._3));
        	target = target.setMultiEdges(targetMultiEdges);
        	features.put(matched.getKey(), target);
        }

        for (var _fps : neighbouring.values()) {
            var fps = List.ofAll(_fps);
            for (FeaturePointer fp : fps) {
                NeighbouringFeature feature = (NeighbouringFeature) features.get(fp);
                feature = feature.setNeighboring(feature.getNeighboring().addAll(fps.remove(fp)));
                features.put(fp, feature);
            }
        }

        io.vavr.collection.HashMap<FeaturePointer, Feature> _features = io.vavr.collection.HashMap.ofAll(features);
        Tile tileDef = new Tile(tileId, _features, tileModifiers);

        features = null;
        this.tileId = null;

        return tileDef;
    }

    public void initFeature(Element xml, Feature feature) {
        if (feature instanceof Field && tileId.startsWith("CO/")) {
            //this is not part of Count capability because it is integral behaviour valid also when capability is off
            feature = ((Field) feature).setAdjoiningCityOfCarcassonne(true);
        }
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            feature = cap.initFeature(state, tileId, feature, xml);
        }

        FeaturePointer fp = feature.getPlaces().get();
        features.put(fp, feature);

        if (feature instanceof  NeighbouringFeature) {
            String[] wagonMoves = xml.getAttribute("wagon-move").split("\\s");
            for (String wagonMove : wagonMoves) {
                if (wagonMove.length() > 0) {
                    var connectedFeatures = neighbouring.get(wagonMove);
                    if (connectedFeatures == null) {
                        connectedFeatures = new ArrayList<>();
                        neighbouring.put(wagonMove, connectedFeatures);
                    }
                    connectedFeatures.add(fp);
                }
            }
        }
    }

    private Map<FeatureModifier<?>, Object> getFeatureModifiers(String featureType, Element el) {
        Map<FeatureModifier<?>, Object> modifiers = HashMap.empty();
        var declaredModifiers = modifiersByType.get(featureType);
        if (declaredModifiers != null) {
            for (FeatureModifier mod : declaredModifiers) {
                if (el.hasAttribute(mod.getName())) {
                    modifiers = modifiers.put(mod, mod.valueOf(el.getAttribute(mod.getName())));
                }
            }
        }
        return modifiers;
    }

    private void processMonasteryElement(Element e) {
        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("monastery", e);
        Monastery monastery = new Monastery(modifiers);
        initFeature(e, monastery);
    }

    private void processRoadElement(Element e, boolean isTunnelActive) {
        Stream<Location> sides = contentAsLocations(e).flatMap(loc -> loc.isInner() ? List.of(loc) : loc.splitToSides());
        //using tunnel argument for two cases, tunnel entrance and tunnel underpass - sides.length distinguish it
        if (sides.size() > 1 && isTunnelActive && attributeBoolValue(e, "tunnel")) {
            sides.forEach(loc -> processRoadElement(Stream.of(loc), e, true));
        } else {
            processRoadElement(sides, e, isTunnelActive);
        }
    }

    private void processRoadElement(Stream<Location> sides, Element e, boolean isTunnelActive) {
        FeaturePointer fp = initFeaturePointer(sides, Road.class);

        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("road", e);
        Road road = new Road(List.of(fp), initOpenEdges(sides), modifiers);

        if (isTunnelActive && attributeBoolValue(e, "tunnel")) {
            road = road.setOpenTunnelEnds(HashSet.of(fp));
        }
        initFeature(e, road);
    }

    private void processCityElement(Element e) {
        Stream<Location> sides = contentAsLocations(e).flatMap(loc -> loc.isInner() ? List.of(loc) : loc.splitToSides());
        FeaturePointer fp = initFeaturePointer(sides, City.class);
        Set<Edge> openEdges = initOpenEdges(sides);

        if (e.hasAttribute("multi-edge")) {
        	Location multiEdgeLoc = attrAsLocation(e, "multi-edge");
        	if (!multiEdgeLoc.isEdge()) {
        		throw new IllegalArgumentException("Multi edge must be side location");
        	}
        	ShortEdge multiEdge = new ShortEdge(Position.ZERO, multiEdgeLoc);
        	multiEdges.add(new Tuple3<ShortEdge, Location, FeaturePointer>(multiEdge, multiEdgeLoc, fp));
        	openEdges = openEdges.add(multiEdge);
        }

        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("city", e);
        City city = new City(List.of(fp), openEdges, modifiers);
        initFeature(e, city);

        if (e.hasAttribute("city-gate")) {
            attrAsLocations(e, "city-gate").forEach(loc -> {
                assert loc.isEdge();
                FeaturePointer gateFp = new FeaturePointer(Position.ZERO, CityGate.class, loc);
                initFeature(null, new CityGate(List.of(gateFp), fp));
            });
        }
    }

    private void processRiverElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer fp = initFeaturePointer(sides, River.class);
        initFeature(e, new River(List.of(fp)));
    }

    private void processFieldElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer fp = initFeaturePointer(sides, Field.class);
        Set<FeaturePointer> adjoiningCities;

        if (e.hasAttribute("city")) {
            //List<City> cities = new ArrayList<>();
            Stream<Location> citiesLocs = attrAsLocations(e, "city");
            citiesLocs = citiesLocs.map(partial -> {
                for(Entry<FeaturePointer, Feature> entry : features.entrySet()) {
                    if (entry.getValue() instanceof City) {
                        Location loc = entry.getKey().getLocation();
                        if (partial.equals(loc) || partial.isPartOf(loc)) {
                            return loc;
                        }
                    }
                }
                throw new IllegalArgumentException(String.format("Unable to match adjoining city %s for tile %s", partial, tileId));
            });
            adjoiningCities = HashSet.ofAll(citiesLocs.map(
                loc -> new FeaturePointer(Position.ZERO, City.class, loc)
            ));
        } else {
            adjoiningCities = HashSet.empty();
        }

        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("field", e);
        Field field = new Field(List.of(fp),  adjoiningCities,  false, modifiers);
        initFeature(e, field);
    }

    private FeaturePointer initFeaturePointer(Stream<Location> sides, Class<? extends Feature> clazz) {
        AtomicReference<Location> locRef = new AtomicReference<>();
        sides.forEach(l -> {
            assert l.isInner() || clazz.equals(Field.class) == l.isFieldEdge() : String.format("Invalid location %s kind for tile %s", l, tileId);
            assert l.intersect(locRef.get()) == null;
            locRef.set(locRef.get() == null ? l : locRef.get().union(l));
        });
        //logger.debug(tile.getId() + "/" + piece.getClass().getSimpleName() + "/"  + loc);
        return new FeaturePointer(Position.ZERO, clazz, locRef.get());
    }

    public static Set<Edge> initOpenEdges(Stream<Location> sides) {
        return HashSet.ofAll(
            sides.filter(Location::isEdge).map(loc -> new Edge(Position.ZERO, loc))
        );
    }
}
