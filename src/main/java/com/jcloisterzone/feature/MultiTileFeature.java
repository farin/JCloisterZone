package com.jcloisterzone.feature;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.feature.visitor.FindMaster;

public abstract class MultiTileFeature extends TileFeature implements Scoreable {

	protected MultiTileFeature[] edges;

	@Override
	public void setLocation(Location location) {
		super.setLocation(location);

		int edgeCount = 0;
		for (Location side : getSides()) {
			if (side.intersect(location) != null) {
				edgeCount++;
			}
		}
		edges = new MultiTileFeature[edgeCount];
	}

	public MultiTileFeature[] getEdges() {
		return edges;
	}

	public boolean containsEdge(MultiTileFeature f) {
		for (MultiTileFeature edge : edges) {
			if (edge == f) return true;
		}
		return false;
	}

	protected Location[] getSides() {
		return Location.sides();
	}

	private int getEdgeIndex(Location edge) {
		int i = 0;
		for (Location side : getSides()) {
			if (side.intersect(getLocation()) != null) {
				if (side.isPartOf(edge)) return i;
				i++;
			}
		}
		throw new IllegalArgumentException("No such edge " + edge);
	}

	public void setEdge(Location loc, MultiTileFeature piece) {
		edges[getEdgeIndex(loc)] = piece;
	}

	public void setAbbeyEdge(Location loc) {
		edges[getEdgeIndex(loc)] = this; //special value
	}


	@Override
	public Feature getMaster() {
		return walk(new FindMaster());		
	}

	@Override
	public <T> T walk(FeatureVisitor<T> visitor) {
		Stack<MultiTileFeature> stack = new Stack<MultiTileFeature>();
		//TODO implement by bit set or marking - this method can be optimized
		Set<MultiTileFeature> visited = new HashSet<>();
		MultiTileFeature previous = null; //little optimization - less touching set
		stack.push(this);
		visited.add(this);
		while(! stack.isEmpty()) {
			MultiTileFeature nextToVisit = stack.pop();
			if (!visitor.visit(nextToVisit)) {
				break;
			}
			for (MultiTileFeature feature : nextToVisit.edges) {
				if (feature != null && feature != nextToVisit && previous != feature && ! visited.contains(feature)) {
					visited.add(feature);
					stack.push(feature);
				}
			}
			previous = nextToVisit;
		}
		return visitor.getResult();
	}

}
