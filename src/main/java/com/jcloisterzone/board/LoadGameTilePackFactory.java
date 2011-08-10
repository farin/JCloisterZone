package com.jcloisterzone.board;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Snapshot;


public class LoadGameTilePackFactory extends TilePackFactory {

	public static final String PLACED_GROUP = "placed";

	private Snapshot snapshot;

	private List<Meeple> preplacedMeeples = Lists.newArrayList();

	class PreplacedTile {
		String tileId;
		Position pos;
		Rotation rot;
		Tile tile;
	}
	PreplacedTile[] preplaced;



	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;

		NodeList nl = snapshot.getTileElements();
		preplaced = new PreplacedTile[nl.getLength()];

		for(int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			preplaced[i] = new PreplacedTile();
			preplaced[i].tileId = el.getAttribute("name");
			preplaced[i].pos = XmlUtils.extractPosition(el);
			preplaced[i].rot = snapshot.extractTileRotation(el);
			preplacedMeeples.addAll(snapshot.extractTileMeeples(el, game, preplaced[i].pos));
		}
	}

	@Override
	protected Map<String, Integer> getDiscardTiles() {
		Map<String, Integer> discard = Maps.newHashMap();
		for(String tileId : snapshot.getDiscardedTiles()) {
			if (discard.containsKey(tileId)) {
				discard.put(tileId, 1 + discard.get(tileId));
			} else {
				discard.put(tileId, 1);
			}
		}
		return discard;
	}

	@Override
	public LinkedList<Position> getPreplacedPositions(String tileId, Element card) {
		return null;
	}

	@Override
	public List<Tile> createTiles(Expansion expansion, String tileId, Element card) {
		List<Tile> result =  super.createTiles(expansion, tileId, card);
		for(PreplacedTile pt : preplaced) {
			if (pt.tileId.equals(tileId)) {
				pt.tile = result.remove(result.size()-1);
			}
			if (result.isEmpty()) {
				break;
			}
		}
		return result;
	}

	public List<Meeple> getPreplacedMeeples() {
		return preplacedMeeples;
	}

	@Override
	protected String getTileGroup(Tile tile, Element card) {
		if (tile.getPosition() != null) {
			return PLACED_GROUP; //special placed group (because all placed must be in active group)
		}
		return super.getTileGroup(tile, card);
	}

	@Override
	public DefaultTilePack createTilePack() {
		DefaultTilePack pack = super.createTilePack();
		for(PreplacedTile pt : preplaced) {
			pt.tile.setRotation(pt.rot);
			pt.tile.setPosition(pt.pos);
			pack.addTile(pt.tile, PLACED_GROUP);
		}
		pack.activateGroup(PLACED_GROUP);
		for(String group : snapshot.getActiveGroups()) {
			if (pack.getGroups().contains(group)) {
				pack.activateGroup(group);
			}
		}
		if (preplaced.length > 0) {
			pack.setCurrentTile(preplaced[preplaced.length-1].tile);
		}
		return pack;
	}

}
