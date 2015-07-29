package com.jcloisterzone.board;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;

public class EdgePattern {

	private Edge[] code = new Edge[4];

	private EdgePattern() { }
	private EdgePattern(Edge[] code) { this.code = code; }

	private static Edge getTileEdgePattern(Tile tile, Location loc) {
		
		if (tile.getRiver() != null && loc.isPartOf(tile.getRiver())) {
			return Edge.RIVER;
		}
		
		Feature f = tile.getFeaturePartOf(loc);
		if (f == null) {
			return Edge.FARM;
		}
		if (f instanceof Road) {
			return Edge.ROAD;
		}
		return Edge.CITY;
	}

	public static EdgePattern forTile(Tile tile) {
		EdgePattern pattern = new EdgePattern();
		for (Location loc : Location.sides()) {
			pattern.code[indexfor (loc)] = getTileEdgePattern(tile, loc);
		}
		return pattern;
	}

	private static int indexfor (Location loc) {
		if (loc == Location.N) return 0;
		if (loc == Location.W) return 1;
		if (loc == Location.S) return 2;
		if (loc == Location.E) return 3;
		throw new IllegalArgumentException();
	}

	public static EdgePattern forEmptyTile(Board board, Position pos) {
		EdgePattern pattern = new EdgePattern();
		for (Location loc : Location.sides()) {
			Tile t = board.get(pos.add(loc));
			int idx = indexfor (loc);
			if (t == null) {
				pattern.code[idx] = Edge.UNKNOWN;
			} else {
				pattern.code[idx] = getTileEdgePattern(t, loc.rev());
			}
		}
		return pattern;
	}

	public Edge at(Location loc) {
		return code[indexfor (loc)];
	}

	public Edge at(Location loc, Rotation rotation) {
		return at(loc.rotateCCW(rotation));
	}

	public int wildcardSize() {
		int size = 0;
		for (int i = 0; i < code.length; i++) {
			if (code[i] == Edge.UNKNOWN) size++;
		}
		return size;
	}

	private EdgePattern switchEdge(int i, Edge ch) {
		Edge[] switched = Arrays.copyOf(code, code.length);
		switched[i] = ch;
		return new EdgePattern(switched);
	}

	public Collection<EdgePattern> fill() {
		//TODO better impl
		if (wildcardSize() == 0) return Collections.singleton(this);
		Queue<EdgePattern> q = new LinkedList<EdgePattern>();
		q.add(this);
		while(q.peek().wildcardSize() > 0) {
			EdgePattern p = q.poll();
			int i = 0;
			while(p.code[i] != Edge.UNKNOWN) i++;
			q.add(switchEdge(i, Edge.RIVER)); // fatsu: not sure what this fill() method is supposed to do. but added this line anyway.
			q.add(switchEdge(i, Edge.ROAD));
			q.add(switchEdge(i, Edge.CITY));
			q.add(switchEdge(i, Edge.FARM));
		}
		return q;
	}

	private Edge[] shift(int shift) {
		Edge[] result = new Edge[4];
		for (int i = 0; i < code.length; i++) {
			result[i] = code[(i+shift)%code.length];
		}
		return result;
	}

	private Edge[] canonize() {
		Edge[] result = code;
		shiftLoop:
		for (int shift = 1; shift < code.length; shift++) {
			Edge[] c = shift(shift);
			for (int i = 0; i < code.length; i++) {
				if (c[i].ordinal() < result[i].ordinal()) {
					result = c;
					continue shiftLoop;
				}
				if (c[i].ordinal() > result[i].ordinal()) {
					break;
				}
			}
		}
		return result;
	}
	
	public boolean isBridgeAllowed(Location bridge, Rotation tileRotation) {
		if (bridge == Location.NS) {
			if (at(Location.N, tileRotation) != Edge.FARM) return false;
			if (at(Location.S, tileRotation) != Edge.FARM) return false;
		} else {
			if (at(Location.W, tileRotation) != Edge.FARM) return false;
			if (at(Location.E, tileRotation) != Edge.FARM) return false;
		}
		return true;
	}
	
	public EdgePattern getBridgePattern(Location bridge) {
		Edge[] bridgeCode = Arrays.copyOf(code, code.length); 
		if (bridge == Location.NS) {
			bridgeCode[0] = Edge.ROAD;
			bridgeCode[2] = Edge.ROAD;
		} else {
			bridgeCode[1] = Edge.ROAD;
			bridgeCode[3] = Edge.ROAD;
		}		
		return new EdgePattern(bridgeCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EdgePattern)) return false;
		EdgePattern that = (EdgePattern) obj;
		return Arrays.equals(that.canonize(), canonize());
	}

	@Override
	public int hashCode() {
		Edge[] c = canonize();
		int hash = 0;
		for (int i = 0; i < c.length; i++) {
			hash = hash * 91 + c[i].ordinal();
		}
		return hash;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < code.length; i++) {
			sb.append(code[i]);
		}
		return sb.toString();
	}

}
