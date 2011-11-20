package com.jcloisterzone.board;

import static com.jcloisterzone.board.XmlUtils.asLocation;
import static com.jcloisterzone.board.XmlUtils.asLocations;
import static com.jcloisterzone.board.XmlUtils.attributeBoolValue;
import static com.jcloisterzone.board.XmlUtils.attributeIntValue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.TileFeature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;


public class TileFactory {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private Tile tile; //context
	private List<Feature> features = Lists.newArrayList();
	private int nextFeatureId = 1;

	private Game game;


	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Tile createTile(String fullId, Element xml, boolean isTunnelActive) {
		Tile tile = new Tile(fullId);
		this.tile = tile;
		tile.setGame(game);

		logger.debug(">>> Creating " + tile.getId());

		NodeList nl;
		nl = xml.getElementsByTagName("cloister");
		for(int i = 0; i < nl.getLength(); i++) {
			processCloisterElement((Element) nl.item(i));
		}
		nl = xml.getElementsByTagName("road");
		for(int i = 0; i < nl.getLength(); i++) {
			processRoadElement((Element) nl.item(i), isTunnelActive);
		}
		nl = xml.getElementsByTagName("city");
		for(int i = 0; i < nl.getLength(); i++) {
			processCityElement((Element) nl.item(i));
		}
		nl = xml.getElementsByTagName("farm");
		for(int i = 0; i < nl.getLength(); i++) {
			processFarmElement((Element) nl.item(i));
		}
		nl = xml.getElementsByTagName("tower");
		for(int i = 0; i < nl.getLength(); i++) {
			processTowerElement((Element) nl.item(i));
		}

		tile.setFeatures(features.toArray(new Feature[features.size()]));
		tile.setSymmetry(TileSymmetry.forTile(tile));
		tile.setEdgePattern(EdgePattern.forTile(tile));

		features.clear();
		this.tile = null; //clear context
		return tile;
	}

	private void processCloisterElement(Element e) {
		Cloister cloister = new Cloister();
		cloister.setId(nextFeatureId++);
		cloister.setTile(tile);
		cloister.setLocation(Location.CLOISTER);
		features.add(cloister);
		game.expansionDelegate().initFeature(tile, cloister, e);
	}

	private void processTowerElement(Element e) {
		Tower tower = new Tower();
		tower.setId(nextFeatureId++);
		tower.setTile(tile);
		tower.setLocation(Location.TOWER);
		features.add(tower);
		game.expansionDelegate().initFeature(tile, tower, e);
	}


	private void processRoadElement(Element e, boolean isTunnelActive) {
		String[] sides = asLocation(e);
		//using tunnel argument for two cases, tunnel entrance and tunnel underpass - sides.lenght distinguish it
		if (sides.length > 1 && isTunnelActive && attributeBoolValue(e, "tunnel")) {
			for(String side: sides) {
				String[] side_as_array = { side };
				processRoadElement(side_as_array, e, true);
			}
		} else {
			processRoadElement(sides, e, isTunnelActive);
		}
	}

	private void processRoadElement(String[] sides, Element e, boolean isTunnelActive) {
		//Road road = new Road(tile, sides.length, sides.length == 1 && attributeBoolValue(e, "tunnel"));
		Road road = new Road();
		road.setId(nextFeatureId++);
		if (isTunnelActive && attributeBoolValue(e, "tunnel")) {
			road.setTunnelEnd(Road.OPEN_TUNNEL);
		}
		initFromDirList(road, sides);
		game.expansionDelegate().initFeature(tile, road, e);
	}

	private void processCityElement(Element e) {
		String[] sides = asLocation(e);
		City c = new City();
		c.setId(nextFeatureId++);
		c.setPennants(attributeIntValue(e, "pennant", 0));
		initFromDirList(c, sides);
		game.expansionDelegate().initFeature(tile, c, e);
	}

	//TODO move expansion specific stuff
	private void processFarmElement(Element e) {
		String[] sides = asLocation(e);
		Farm farm = new Farm();
		farm.setId(nextFeatureId++);
		if (e.hasAttribute("city")) {
			List<City> cities = Lists.newArrayList();
			String[] citiesLocs = asLocations(e, "city");
			for(int j = 0; j < citiesLocs.length; j++) {
				Location d = Location.valueOf(citiesLocs[j]);
				for(Feature p : features) {
					if (p instanceof City) {
						if (d.isPartOf(p.getLocation())) {
							cities.add((City) p);
							break;
						}
					}
				}
			}
			farm.setAdjoiningCities(cities.toArray(new City[cities.size()]));
		}
		if (attributeBoolValue(e, "pig")) {
			//for river is pig herd always present
			if (game.hasRule(CustomRule.PIG_HERD_ON_GQ_FARM) || tile.getId() != "GQ.F") {
				farm.setPigHerd(true);
			}
		}
		initFromDirList(farm, sides);
		game.expansionDelegate().initFeature(tile, farm, e);
	}

	private void initFromDirList(TileFeature piece, String[] sides) {
		Location loc = null;
		for(int i = 0; i < sides.length; i++) {
			Location l = Location.valueOf(sides[i]);
			assert !(piece instanceof Farm ^ l.isFarmLocation()) : "Invalid location kind for tile "+tile.getId();
			loc = loc == null ? l : loc.union(l);
		}
		//logger.debug(tile.getId() + "/" + piece.getClass().getSimpleName() + "/"  + loc);
		piece.setTile(tile);
		piece.setLocation(loc);
		features.add(piece);
	}

}
