package com.jcloisterzone.board;

import static com.jcloisterzone.XMLUtils.attrAsLocations;
import static com.jcloisterzone.XMLUtils.attributeBoolValue;
import static com.jcloisterzone.XMLUtils.attributeIntValue;
import static com.jcloisterzone.XMLUtils.contentAsLocations;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.River;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;


public class TileBuilder {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private java.util.Map<Location, Feature> features;
    private String tileId;

    private GameState state;


    public GameState getGameState() {
        return state;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    public TileDefinition createTile(Expansion expansion, String tileId, Element xml, boolean isTunnelActive) {

        features = new java.util.HashMap<>();
        this.tileId = tileId;

        logger.debug("Creating " + tileId);

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
        nl = xml.getElementsByTagName("farm");
        for (int i = 0; i < nl.getLength(); i++) {
            processFarmElement((Element) nl.item(i));
        }
        nl = xml.getElementsByTagName("tower");
        for (int i = 0; i < nl.getLength(); i++) {
            processTowerElement((Element) nl.item(i));
        }
        nl = xml.getElementsByTagName("river");
        for (int i = 0; i < nl.getLength(); i++) {
            processRiverElement((Element) nl.item(i));
        }

        //IMMUTABLE TODO
        //TODO Count
//        for (Feature f : extendFeatures(tile)) {
//            TileFeature tileFeature = (TileFeature) f;
//            tileFeature.setId(game.idSequnceNextVal());
//            tileFeature.setTile(tile);
//            features.add(tileFeature);
//        }

        io.vavr.collection.HashMap<Location, Feature> _features = io.vavr.collection.HashMap.ofAll(features);
        TileDefinition tileDef = new TileDefinition(expansion, tileId, _features);

        features = null;
        tileId = null;

        return tileDef;
    }

    public Feature initFeature(String tileId, Feature feature, Element xml) {
        if (feature instanceof Farm && tileId.startsWith("CO.")) {
            //this is not part of Count capability because it is integral behaviour valid also when capability is off
            feature = ((Farm) feature).setAdjoiningCityOfCarcassonne(true);
        }
        for (Capability<?> cap: state.getCapabilities().toSeq()) {
            feature = cap.initFeature(state, tileId, feature, xml);
        }
        return feature;
    }

//    public List<Feature> extendFeatures(String tileId) {
//        List<Feature> result = List.empty();
//        for (Capability cap: getCapabilities()) {
//            result.appendAll(cap.extendFeatures(tileId));
//        }
//        return result;
//    }

    private void processCloisterElement(Element e) {
        Cloister cloister = new Cloister(
            List.of(new FeaturePointer(Position.ZERO, Location.CLOISTER))
        );
        cloister = (Cloister) initFeature(tileId, cloister, e);
        features.put(Location.CLOISTER, cloister);

    }

    private void processTowerElement(Element e) {
        Tower tower = new Tower(
            List.of(new FeaturePointer(Position.ZERO, Location.TOWER))
        );
        tower = (Tower) initFeature(tileId, tower, e);
        features.put(Location.TOWER, tower);
    }

    private void processRoadElement(Element e, boolean isTunnelActive) {
        //List<Location> sides = List.ofAll(contentAsLocations(e))
        Stream<Location> sides = contentAsLocations(e);
        //using tunnel argument for two cases, tunnel entrance and tunnel underpass - sides.length distinguish it
        if (sides.size() > 1 && isTunnelActive && attributeBoolValue(e, "tunnel")) {
            sides.forEach(loc -> {
                processRoadElement(Stream.of(loc), e, true);
            });
        } else {
            processRoadElement(sides, e, isTunnelActive);
        }
    }

    private void processRoadElement(Stream<Location> sides, Element e, boolean isTunnelActive) {
        FeaturePointer fp = initFeaturePointer(sides, Road.class);
        Road road = new Road(
            List.of(fp),
            initOpenEdges(sides)
        );

        if (isTunnelActive && attributeBoolValue(e, "tunnel")) {
            road = road.setOpenTunnelEnds(HashSet.of(fp));
        }

        road = (Road) initFeature(tileId, road, e);
        features.put(fp.getLocation(), road);
    }

    private void processCityElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer place = initFeaturePointer(sides, City.class);

        City city = new City(
            List.of(place),
            initOpenEdges(sides),
            attributeIntValue(e, "pennant", 0)
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

    //TODO move expansion specific stuff
    private void processFarmElement(Element e) {
        Stream<Location> sides = contentAsLocations(e);
        FeaturePointer place = initFeaturePointer(sides, Farm.class);
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

        Farm farm = new Farm(
            List.of(place),
            adjoiningCities
        );

        farm = (Farm) initFeature(tileId, farm, e);
        features.put(place.getLocation(), farm);
    }

    private FeaturePointer initFeaturePointer(Stream<Location> sides, Class<? extends Feature> clazz) {
        AtomicReference<Location> locRef = new AtomicReference<Location>();
        sides.forEach(l -> {
            assert !(clazz.equals(Farm.class) ^ l.isFarmLocation()) : String.format("Invalid location %s kind for tile %s", l, tileId);
            assert l.intersect(locRef.get()) == null;
            locRef.set(locRef.get() == null ? l : locRef.get().union(l));
        });
        //logger.debug(tile.getId() + "/" + piece.getClass().getSimpleName() + "/"  + loc);
        return new FeaturePointer(Position.ZERO, locRef.get());
    }

    public static Set<Edge> initOpenEdges(Stream<Location> sides) {
        return HashSet.ofAll(
            sides.filter(loc -> loc.isEdgeLocation()).map(loc -> new Edge(Position.ZERO, loc))
        );
    }
}
