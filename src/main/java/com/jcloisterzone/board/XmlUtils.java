package com.jcloisterzone.board;

import org.w3c.dom.Element;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.SnapshotCorruptedException;


public class XmlUtils {

	public static String[] asLocation(Element e) {
		return e.getFirstChild().getNodeValue().trim().split("\\s+");
	}

	public static String[] asLocations(Element e, String attr) {
		return e.getAttribute(attr).trim().split("\\s+");
	}

	public static String getTileId(Expansion expansion, Element xml) {
		return expansion.getCode() + "." + xml.getAttribute("id");
	}

	public static  Location union(String[] locations) {
		Location u = null;
		for(String locStr : locations) {
			Location loc = Location.valueOf(locStr);
			u = loc.union(u);
		}
		return u;
	}

	public static boolean attributeBoolValue(Element e, String attr) {
		return e.getAttribute(attr).equals("yes") || e.getAttribute(attr).equals("true") ;
	}

	public static int attributeIntValue(Element e, String attr) {
		return attributeIntValue(e, attr, null);
	}

	public static int attributeIntValue(Element e, String attr, Integer defaultValue) {
		if (! e.hasAttribute(attr)) {
			return defaultValue;
		}
		if (e.getAttribute(attr).equals("yes") || e.getAttribute(attr).equals("true")) {
			return 1;
		}
		return Integer.parseInt(e.getAttribute(attr));
	}

	public static String attributeStringValue(Element e, String attr, String defaultValue) {
		if (! e.hasAttribute(attr)) {
			return defaultValue;
		}
		return e.getAttribute(attr);
	}

	// Snapshot xml utils

	public static Class<?> classForName(String className) throws SnapshotCorruptedException {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new SnapshotCorruptedException(e);
		}
	}

	public static void injectPosition(Element el, Position p) {
		el.setAttribute("x", "" + p.x);
		el.setAttribute("y", "" + p.y);
	}

	public static Position extractPosition(Element el) {
		int x = Integer.parseInt(el.getAttribute("x"));
		int y = Integer.parseInt(el.getAttribute("y"));
		return new Position(x, y);
	}
}
