package com.jcloisterzone.ui.theme;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.feature.Feature;


public class AreaProvider {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, Area> areas; //key is descriptor
	private final Map<String, Area> substraction; //key tile ID
	private final Set<String> complementFarms;
	
	private static final Area BRIDGE_AREA_NS, BRIDGE_AREA_WE;
	
	static {
		Area a = new Area(new Rectangle(400, 0, 200, 1000));
		a.subtract(new Area(new Ellipse2D.Double(300, 150, 200, 700)));
		BRIDGE_AREA_NS = new Area(a);
		a.transform(Rotation.R270.getAffineTransform(TileTheme.NORMALIZED_SIZE));
		BRIDGE_AREA_WE = a;		
	}

	public AreaProvider(URL areaDef) {
		ImmutableMap.Builder<String, Area> areaBuilder = new ImmutableMap.Builder<String, Area>();
		ImmutableMap.Builder<String, Area> substractionBuilder = new ImmutableMap.Builder<String, Area>();
		ImmutableSet.Builder<String> complementBuilder = new ImmutableSet.Builder<String>();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Element display = docBuilder.parse(areaDef.openStream()).getDocumentElement();
			NodeList nl = display.getElementsByTagName("area");
			for(int i = 0; i < nl.getLength(); i++) {
				processArea((Element) nl.item(i), areaBuilder, substractionBuilder);
			}
			nl = display.getElementsByTagName("complement-farm");
			for(int i = 0; i < nl.getLength(); i++) {
				processComplementFarm((Element) nl.item(i), complementBuilder);
			}

		} catch (Exception e) {
			logger.error("Unable to read theme definitions from " + areaDef.toString(), e);
			System.exit(1);
		}
		areas = areaBuilder.build();
		complementFarms = complementBuilder.build();
		substraction = substractionBuilder.build();
	}

	private Shape loadRectangle(Element area) {
		NodeList nl = area.getElementsByTagName("rectangle");
		Element rect = (Element) nl.item(0);
		int x, y, h, w;
		x = Integer.parseInt(rect.getAttribute("x"));
		y = Integer.parseInt(rect.getAttribute("y"));
		h = Integer.parseInt(rect.getAttribute("h"));
		w = Integer.parseInt(rect.getAttribute("w"));
		return new Rectangle(x, y, w, h);
	}

	private Shape loadCircle(Element area) {
		NodeList nl = area.getElementsByTagName("circle");
		Element circle = (Element) nl.item(0);
		int x, y, r;
		x = Integer.parseInt(circle.getAttribute("x"));
		y = Integer.parseInt(circle.getAttribute("y"));
		r = Integer.parseInt(circle.getAttribute("r"));
		return new Ellipse2D.Double(x-r,y-r,2*r,2*r);
	}

	private Shape loadPolygon(Element area) {
		String xpoints = area.getElementsByTagName("x-points").item(0)
				.getChildNodes().item(0).getNodeValue();
		String ypoints = area.getElementsByTagName("y-points").item(0)
				.getChildNodes().item(0).getNodeValue();
		String[] x = xpoints.split(",");
		String[] y = ypoints.split(",");
		if (x.length != y.length) throw new IllegalArgumentException("X points length does not match Y points length");
		Polygon p = new Polygon();
		for (int i = 0; i < x.length; i++) {
			int ix, iy;
			ix = Integer.parseInt(x[i].trim());
			iy = Integer.parseInt(y[i].trim());
			if (ix < 0 || ix > TileTheme.NORMALIZED_SIZE || iy < 0 || iy > TileTheme.NORMALIZED_SIZE) {
				throw new IllegalArgumentException("Invalid number range");
			}
			p.addPoint(ix, iy);
		}
		return p;
	}

	private String normalizeDescriptor(String descriptor) {
		String trimmed = descriptor.trim();
		String[] tokens = trimmed.split(" ");
		if (tokens[2].contains(",")) {
			StringBuilder b = new StringBuilder();
			b.append(tokens[0]).append(' ');
			b.append(tokens[1]).append(' ');
			b.append(XmlUtils.union(tokens[2].split(",")));
			//logger.info("Normalize " + descriptor + " to " + b.toString());
			return b.toString();
		}
		return trimmed;
	}

	private Area areaForRotation(Area[] areas, Element xml, Shape shape) {
		Rotation rotation;
		if (xml.hasAttribute("rotate")) {
			rotation = Rotation.values()[Integer.parseInt(xml.getAttribute("rotate"))];
		} else {
			rotation = Rotation.R0;
		}
		Area area = areas[rotation.ordinal()];
		if (area == null) {
			area= (new Area(shape)).createTransformedArea(rotation.getAffineTransform(TileTheme.NORMALIZED_SIZE));
			areas[rotation.ordinal()] = area;
		}
		return area;
	}

	private void processArea(Element xml, ImmutableMap.Builder<String, Area> areaBuilder, ImmutableMap.Builder<String, Area> substractionBuilder) {
		Shape shape = null;
		if (xml.getElementsByTagName("x-points").getLength() > 0) {
			shape = loadPolygon(xml);
		} else if (xml.getElementsByTagName("rectangle").getLength() > 0) {
			shape = loadRectangle(xml);
		} else if (xml.getElementsByTagName("circle").getLength() > 0) {
			shape = loadCircle(xml);
		} else {
			throw new IllegalArgumentException("Invalid area. No shape defined.");
		}
		NodeList nl = xml.getElementsByTagName("apply");
		Area[] areas = new Area[4];
		for(int i = 0; i < nl.getLength(); i++) {
			Element applyElemenet = (Element) nl.item(i);
			String descriptor = normalizeDescriptor(applyElemenet.getFirstChild().getNodeValue());
			//System.out.println(applyElemenet.getFirstChild().getNodeValue() + " >>> " + descriptor);
			areaBuilder.put(descriptor, areaForRotation(areas, applyElemenet, shape));
		}
		nl = xml.getElementsByTagName("substract");
		for(int i = 0; i < nl.getLength(); i++) {
			Element substractElement = (Element) nl.item(i);
			String tileId = substractElement.getFirstChild().getNodeValue().trim();
			substractionBuilder.put(tileId, areaForRotation(areas, substractElement, shape));
		}
	}

	private void processComplementFarm(Element xml, ImmutableSet.Builder<String> builder) {
		NodeList nl = xml.getElementsByTagName("apply");
		for(int i = 0; i < nl.getLength(); i++) {
			Element apply = (Element) nl.item(i);
			builder.add(normalizeDescriptor(apply.getFirstChild().getNodeValue()));
		}
	}

	private String getDescriptor(Tile tile, Rotation rotation, String  featureName, Location loc) {
		StringBuilder desc = new StringBuilder();
		if (tile == null) {
			desc.append("* ");
		} else {
			desc.append(tile.getId()).append(" ");
		}
		desc.append(featureName).append(" ");
		desc.append(loc.rotateCCW(rotation).toString());
		return desc.toString();
	}

	//copy from FigurePositionProvider - TODO maybe merge

	public Area getArea(Tile tile, Feature piece, Location loc) {
		return getArea(tile, piece.getClass(), loc);
	}

	public Area getArea(Tile tile, Class<? extends Feature> featureClass, Location loc) {
		return getArea(tile, featureClass.getSimpleName().toUpperCase(), loc);
	}

	private Area getArea(Tile tile, String featureName, Location loc) {
		Rotation tileRotation = tile.getRotation();							
		if (featureName.equals("BRIDGE")) {
			Area a =  getBridgeArea(loc.rotateCCW(tileRotation));
			//bridge is independent on tile rotation
			if ((loc == Location.WE && (tileRotation == Rotation.R90 || tileRotation == Rotation.R180)) ||
				(loc == Location.NS && (tileRotation == Rotation.R180 || tileRotation == Rotation.R270))) {
				a = new Area(a);
				a.transform(Rotation.R180.getAffineTransform(TileTheme.NORMALIZED_SIZE));
			}
			return a;
		}
		String descriptor = getDescriptor(tile, tileRotation, featureName, loc);
		Area area = areas.get(descriptor);
		if (area == null) {
			//try generic descriptor
			area = areas.get(getDescriptor(null, tileRotation, featureName, loc));
			if (area == null) {
				logger.error("No shape defined for <" + descriptor + ">");
				//just return some area - no sense in values
				area = new Area(new Rectangle(TileTheme.NORMALIZED_SIZE/4, TileTheme.NORMALIZED_SIZE/4, TileTheme.NORMALIZED_SIZE/2, TileTheme.NORMALIZED_SIZE/2));
			}
		}
		return area;
	}
	
	public Area getBridgeArea(Location loc) {
		//TODO use display.xml to define areas ? (but it is too complicated shape) 
		if (loc == Location.NS) return BRIDGE_AREA_NS; 
		if (loc == Location.WE) return BRIDGE_AREA_WE;
		throw new IllegalArgumentException("Incorrect location");
	}

	public Area getSubstractionArea(Tile tile) {
		return substraction.get(tile.getId());
	}

	public boolean isFarmComplement(Tile tile, Location d) {
		if (complementFarms.contains(getDescriptor(tile, tile.getRotation(), "FARM", d))) return true;
		if (complementFarms.contains(getDescriptor(null, tile.getRotation(), "FARM", d))) return true;
		return false;
	}
}