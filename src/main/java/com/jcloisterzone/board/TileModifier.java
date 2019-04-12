package com.jcloisterzone.board;

/**
 *
 * Represents a tag that can be attached to {@link Tile}s. A tag is usually associated with a special
 * behaviour.
 */
public class TileModifier {

	private String name;

	public TileModifier(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
