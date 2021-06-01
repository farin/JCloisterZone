package com.jcloisterzone.board;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.Capability;
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

    private static final FeatureModifier[] CITY_MODIFIERS = new FeatureModifier[] { City.PENNANTS, City.CATHEDRAL, City.PRINCESS, City.BESIEGED, City.DARMSTADTIUM };
    private static final FeatureModifier[] ROAD_MODIFIERS = new FeatureModifier[] { Road.INN, Road.LABYRINTH };

    private java.util.List<FeatureModifier> externalModifiers;
    private java.util.Map<String, java.util.List<FeatureModifier>> modifiersByType;

    private java.util.Map<Location, Feature> features;
    private java.util.List<Tuple3<ShortEdge, Location, FeaturePointer>> multiEdges; //Edge, edge location, target feature (which is declared without edge)
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

    public Tile createTile(String tileId, Vector<Element> tileElements, boolean isTunnelActive) {

        features = new java.util.HashMap<>();
        multiEdges = new java.util.ArrayList<>();
        this.tileId = tileId;

        logger.debug("Creating " + tileId);

        for (Element xml : tileElements) {
            NodeList nl;
            nl = xml.getElementsByTagName("cloister");
            for (int i = 0; i < nl.getLength(); i++) {
                processCloisterElement((Element) nl.item(i));
            }
            nl = xml.getElementsByTagName("road");
            for (int i = 0; i < nl.getLength(); i++) {
                processRoadElement((Element) nl.item(i), isTunnelActive);
            }
            nl = xml.getElementsByTagName("city");
            for (int i = 0; i < nl.getLength(); i++) {
                processCityElement((Element) nl.item(i));
            }
            nl = xml.getElementsByTagName("field");
            for (int i = 0; i < nl.getLength(); i++) {
                processFieldElement((Element) nl.item(i));
            }
            nl = xml.getElementsByTagName("tower");
            for (int i = 0; i < nl.getLength(); i++) {
                processTowerElement((Element) nl.item(i));
            }
            nl = xml.getElementsByTagName("river");
            for (int i = 0; i < nl.getLength(); i++) {
                processRiverElement((Element) nl.item(i));
            }
        }

        for (Tuple3<ShortEdge, Location, FeaturePointer> multiEdge: multiEdges) {
        	java.util.Map.Entry<Location, Feature> matched = null;
        	for (java.util.Map.Entry<Location, Feature> entry : features.entrySet()) {
        		if (multiEdge._2.isPartOf(entry.getKey())) {
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

        io.vavr.collection.HashMap<Location, Feature> _features = io.vavr.collection.HashMap.ofAll(features);
        Tile tileDef = new Tile(tileId, _features);

        features = null;
        this.tileId = null;

        return tileDef;
    }

    public Feature initFeature(String tileId, Feature feature, Element xml) {
        if (feature instanceof Field && tileId.startsWith("CO/")) {
            //this is not part of Count capability because it is integral behaviour valid also when capability is off
            feature = ((Field) feature).setAdjoiningCityOfCarcassonne(true);
        }
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            feature = cap.initFeature(state, tileId, feature, xml);
        }
        return feature;
    }

    private Map<FeatureModifier<?>, Object> getFeatureModifiers(String featureType, Element el) {
        Map<FeatureModifier<?>, Object> modifiers = HashMap.empty();
        for (FeatureModifier mod: modifiersByType.get("road")) {
            if (el.hasAttribute(mod.getName())) {
                modifiers = modifiers.put(mod, mod.valueOf(el.getAttribute(mod.getName())));
            }
        }
        return modifiers;
    }

    private void processCloisterElement(Element e) {
        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("cloister", e);
        Cloister cloister = new Cloister(modifiers);
        cloister = (Cloister) initFeature(tileId, cloister, e);
        features.put(Location.CLOISTER, cloister);

    }

    private void processTowerElement(Element e) {
        Tower tower = new Tower();
        tower = (Tower) initFeature(tileId, tower, e);
        features.put(Location.TOWER, tower);
    }

    private void processRoadElement(Element e, boolean isTunnelActive) {
        //List<Location> sides = List.ofAll(contentAsLocations(e))
        Stream<Location> sides = contentAsLocations(e);
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

        road = (Road) initFeature(tileId, road, e);
        features.put(fp.getLocation(), road);
    }

    private void processCityElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer place = initFeaturePointer(sides, City.class);
        Set<Edge> openEdges = initOpenEdges(sides);

        if (e.hasAttribute("multi-edge")) {
        	Location multiEdgeLoc = attrAsLocation(e, "multi-edge");
        	if (!multiEdgeLoc.isEdge()) {
        		throw new IllegalArgumentException("Multi edge must be side location");
        	}
        	ShortEdge multiEdge = new ShortEdge(Position.ZERO, multiEdgeLoc);
        	multiEdges.add(new Tuple3<ShortEdge, Location, FeaturePointer>(multiEdge, multiEdgeLoc, place));
        	openEdges = openEdges.add(multiEdge);
        }

        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("city", e);
        City city = new City(
            List.of(place),
            openEdges, modifiers
        );

        city = (City) initFeature(tileId, city, e);
        features.put(place.getLocation(), city);
    }

    private void processRiverElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer place = initFeaturePointer(sides, River.class);

        River river = new River(
            List.of(place)
        );

        river = (River) initFeature(tileId, river, e);
        features.put(place.getLocation(), river);
    }

    private void processFieldElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer place = initFeaturePointer(sides, Field.class);
        Set<FeaturePointer> adjoiningCities;

        if (e.hasAttribute("city")) {
            //List<City> cities = new ArrayList<>();
            Stream<Location> citiesLocs = attrAsLocations(e, "city");
            citiesLocs = citiesLocs.map(partial -> {
                for(Entry<Location, Feature> entry : features.entrySet()) {
                    if (entry.getValue() instanceof City) {
                        Location loc = entry.getKey();
                        if (partial.isPartOf(loc)) {
                            return loc;
                        }
                    }
                }
                throw new IllegalArgumentException(String.format("Unable to match adjoining city %s for tile %s", partial, tileId));
            });
            adjoiningCities = HashSet.ofAll(citiesLocs.map(
                loc -> new FeaturePointer(Position.ZERO, loc)
            ));
        } else {
            adjoiningCities = HashSet.empty();
        }

        Map<FeatureModifier<?>, Object> modifiers = getFeatureModifiers("field", e);
        Field field = new Field(List.of(place),  adjoiningCities,  false, modifiers);

        field = (Field) initFeature(tileId, field, e);
        features.put(place.getLocation(), field);
    }

    private FeaturePointer initFeaturePointer(Stream<Location> sides, Class<? extends Feature> clazz) {
        AtomicReference<Location> locRef = new AtomicReference<>();
        sides.forEach(l -> {
            assert l.isInner() || clazz.equals(Field.class) == l.isFieldEdge() : String.format("Invalid location %s kind for tile %s", l, tileId);
            assert l.intersect(locRef.get()) == null;
            locRef.set(locRef.get() == null ? l : locRef.get().union(l));
        });
        //logger.debug(tile.getId() + "/" + piece.getClass().getSimpleName() + "/"  + loc);
        return new FeaturePointer(Position.ZERO, locRef.get());
    }

    public static Set<Edge> initOpenEdges(Stream<Location> sides) {
        return HashSet.ofAll(
            sides.filter(Location::isEdge).map(loc -> new Edge(Position.ZERO, loc))
        );
    }
}
