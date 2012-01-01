package com.jcloisterzone.ui.theme;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.legacy.FigurePositionProvider;

public class TileTheme extends Theme {

	//TODO merge with legacy tile info provider

	public static final int NORMALIZED_SIZE = 1000;

	private AreaProvider areaProvider;

	//legacy
	private FigurePositionProvider figurePositionProvider;


	public static boolean isBrightColor(Color c) {
		return c.getRed() > 192 && c.getGreen() > 192 && c.getBlue() < 64;
	}


	public TileTheme(Client client) {
		super("theme-tiles", client);
		areaProvider = new AreaProvider(getResource("display.xml"));

		//legacy TODO remove
		figurePositionProvider = new FigurePositionProvider(this);
	}

	public AreaProvider getAreaProvider() {
		return areaProvider;
	}

	public Image getEmptyImage() {
		return getImage("empty.jpg");
	}

	public Image getTileImage(String tileId) {
		//TODO cache directly under tileID without pathname conversion
		String name = tileId.substring(0, 2) + "/" + tileId.substring(3) + ".jpg";
		return getImage(name);
	}

	public ImmutablePoint getFigurePlacement(Tile tile, Meeple m) {
		if (m instanceof Barn) {
			return getBarnPlacement(m.getLocation());
		}
		return getFigurePlacement(tile, m.getFeature().getClass(), m.getLocation());
	}

	public ImmutablePoint getFigurePlacement(Tile tile, Class<? extends Feature> piece, Location loc) {
		return figurePositionProvider.getFigurePlacement(tile, piece, loc);
	}

	public ImmutablePoint getBarnPlacement(Location loc) {
		if (loc.intersect(Location.NL.union(Location.WR)) != null) return new ImmutablePoint(0, 0);
		if (loc.intersect(Location.NR.union(Location.EL)) != null) return new ImmutablePoint(100, 0);
		if (loc.intersect(Location.SL.union(Location.ER)) != null) return new ImmutablePoint(100, 100);
		if (loc.intersect(Location.SR.union(Location.WL)) != null) return new ImmutablePoint(0, 100);
		throw new IllegalArgumentException("Corner location expected");
	}


	public Area getMeepleTileArea(Tile tile, int size, Location d) {
		Map<Location, Area> areas = getMeepleTileAreas(tile, size, Collections.singleton(d));
		if (areas.isEmpty()) return null;
		return areas.values().iterator().next();
	}

	public Map<Location, Area> getMeepleTileAreas(Tile tile, int size, Set<Location> locations) {
		Map<Location, Area> areas = Maps.newHashMap();
		Area subsBridge = getBaseRoadAndCitySubstractions(tile);
		Area subsRoadCity = new Area(subsBridge);
		substractBridge(subsRoadCity, tile);
		Area subsFarm = getFarmSubstractions(tile);

		for(Feature piece : tile.getFeatures()) {
			Location loc = piece.getLocation();
			if (! locations.contains(loc)) {
				continue;
			}

			if (piece instanceof Farm) {
				areas.put(loc, getFarmArea(loc, tile, subsFarm));
				continue;
			}
			Area a = areaProvider.getArea(tile, piece, loc);
			if (piece instanceof City || piece instanceof Road) {
				Area subs = subsRoadCity;
				if (piece instanceof Bridge) {
					subs = subsBridge;
				}
				if (! subs.isEmpty()) {
					a = new Area(a); //copy to preserve original
					a.subtract(subs);
				}
			}
			areas.put(loc, a);
		}
		Map<Location, Area> transformed = Maps.newHashMap();

		AffineTransform transform1;
		if (size == NORMALIZED_SIZE) {
			transform1 = new AffineTransform();
		} else {
			double ratio = size/(double)NORMALIZED_SIZE;
			transform1 = AffineTransform.getScaleInstance(ratio,ratio);
		}
		//TODO rotation - 3 rotations are done - Location rotation, getArea and this affine
		AffineTransform transform2 = tile.getRotation().getAffineTransform(size);

		for(Entry<Location, Area> entry : areas.entrySet()) {
			Area a = entry.getValue();
			a = a.createTransformedArea(transform1);
			a = a.createTransformedArea(transform2);
			transformed.put(entry.getKey(), a);
		}
		return transformed;
	}

	public Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
		Map<Location, Area> result = Maps.newHashMap();
		for(Location corner : corners) {
			int r = size/2;
			Area a = new Area(new Ellipse2D.Double(-r,-r,2*r,2*r));
			if (corner.isPartOf(Location.NR.union(Location.EL))) a.transform(Rotation.R90.getAffineTransform(size));
			if (corner.isPartOf(Location.SL.union(Location.ER))) a.transform(Rotation.R180.getAffineTransform(size));
			if (corner.isPartOf(Location.SR.union(Location.WL))) a.transform(Rotation.R270.getAffineTransform(size));
			result.put(corner, a);
		}
		return result;
	}
	
	public Map<Location, Area> getBridgeAreas(int size, Set<Location> locations) {
		Map<Location, Area> result = Maps.newHashMap();		
		for(Location loc : locations) {			
			result.put(loc, getBridgeArea(size, loc));
		}
		return result;
	}
	
	//TODO move to Area Provider ???
	public Area getBridgeArea(int size, Location loc) {
		
		AffineTransform transform1;
		if (size == NORMALIZED_SIZE) {
			transform1 = new AffineTransform();
		} else {
			double ratio = size/(double)NORMALIZED_SIZE;
			transform1 = AffineTransform.getScaleInstance(ratio,ratio);
		}				
		return areaProvider.getBridgeArea(loc).createTransformedArea(transform1);		
	}
	
	private void substractBridge(Area substractions, Tile tile) {
		Bridge bridge = tile.getBridge(); 
		if (bridge != null) {
			Area area;
			area = areaProvider.getArea(tile, Bridge.class, bridge.getLocation());
			substractions.add(area);
		}
	}	

	private Area getBaseRoadAndCitySubstractions(Tile tile) {
		Area sub = new Area();
		if (tile.getTower() != null) {
			sub.add(areaProvider.getArea(tile, Tower.class, Location.TOWER));
		}		
		Area substraction = areaProvider.getSubstractionArea(tile);
		if (substraction != null) {
			sub.add(substraction);
		}		
		return sub;
	}

	private Area getFarmSubstractions(Tile tile) {
		Area sub = new Area();
		for(Feature piece : tile.getFeatures()) {
			if (! (piece instanceof Farm)) {
				Area area = areaProvider.getArea(tile, piece.getClass(), piece.getLocation());
				sub.add(area);
			}
		}
		Area substraction = areaProvider.getSubstractionArea(tile);
		if (substraction != null) {
			sub.add(substraction);
		}		
		return sub;
	}


	private Area getFarmArea(Location farm, Tile tile, Area sub) {
		Area base;
		if (areaProvider.isFarmComplement(tile, farm)) { //is complement farm
			base = new Area(new Rectangle(0,0, NORMALIZED_SIZE, NORMALIZED_SIZE));
			for(Feature piece : tile.getFeatures()) {
				if (piece instanceof Farm && ! piece.getLocation().isRotationOf(farm)) {
					Area area = areaProvider.getArea(tile, Farm.class, piece.getLocation());
					base.subtract(area);
				}
			}
		} else {
			base = areaProvider.getArea(tile, Farm.class, farm);
			base = new Area(base); //copy area to not substract from original
		}
		if (! sub.isEmpty()) {
			base.subtract(sub);
		}
		return base;
	}



}
