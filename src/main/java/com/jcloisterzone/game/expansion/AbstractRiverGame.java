package com.jcloisterzone.game.expansion;

import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.game.ExpandedGame;


public abstract class AbstractRiverGame extends ExpandedGame {

	@Override
	public void initTile(Tile tile, Element xml) {
		NodeList nl;
		nl = xml.getElementsByTagName("river");
		for(int i = 0; i < nl.getLength(); i++) {
			Location river = XmlUtils.union(XmlUtils.asLocation((Element) nl.item(i)));
			tile.setRiver(river);
			if (tile.getSymmetry() != TileSymmetry.NONE) {
				if (tile.getRiver().isRotationOf(Location.WE)) {
					tile.setSymmetry(TileSymmetry.S2);
				} else {
					tile.setSymmetry(TileSymmetry.NONE);
				}
			}
		}
	};



	abstract protected String getLakeId();

	@Override
	public void begin() {
		getTilePack().deactivateGroup("default");
		getTilePack().activateGroup("river-start");
	}

	public void activateNonRiverTiles() {
		getTilePack().activateGroup("default");
		getTilePack().deactivateGroup("river");
		Tile lake = getTilePack().drawTile(TilePack.INACTIVE_GROUP, getLakeId());
		getBoard().checkMoves(lake);
		Entry<Position, Set<Rotation>> entry = getBoard().getAvailablePlacements().entrySet().iterator().next();
		lake.setRotation(entry.getValue().iterator().next());
		getBoard().add(lake, entry.getKey());
		game.fireGameEvent().tilePlaced(lake);
	}

	@Override
	public void turnCleanUp() {
		if (getTile().getRiver() == null) return;
		if (getTilePack().isEmpty()) {
			if (getTilePack().isGroupActive("river")) {
				activateNonRiverTiles();
			} else {
				getTilePack().deactivateGroup("river-start");
				getTilePack().activateGroup("river");
			}
		}
	}

	@Override
	public boolean checkMove(Tile tile, Position p) {
		if (tile.getRiver() == null) return true;
		for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {

			//check river connection
			Location tileRelativePosition = e.getKey();
			Tile placedTile = e.getValue();
			boolean r1 = tileRelativePosition.rotateCCW(tile.getRotation()).isPartOf(tile.getRiver());
			boolean r2 = tileRelativePosition.rotateCCW(placedTile.getRotation()).rev().isPartOf(placedTile.getRiver());
			if (!(r1 & r2)) return false;

			//check U-turn
			Location continueRiver = tile.getRiver().rotateCW(tile.getRotation()).substract(tileRelativePosition);
			if (continueRiver == Location.INNER_FARM) return true; //lake
			for(Location continueSide: Location.sides()) { //split beacuse of river fork
				if (continueRiver.intersect(continueSide) == null) continue;
				Position pCheck = p.add(continueSide).add(continueSide.rotateCW(Rotation.R90));
				if (getBoard().get(pCheck) != null) return false;
				pCheck = p.add(continueSide).add(continueSide.rotateCCW(Rotation.R90));
				if (getBoard().get(pCheck) != null) return false;
				pCheck = p.add(continueSide).add(continueSide);
				if (getBoard().get(pCheck) != null) return false;
				//also forbid fork "parallel river"
				Tile next = getBoard().get(p.add(continueSide.rotateCW(Rotation.R90)));
				if (next != null && next.getRiver().rotateCW(next.getRotation()).intersect(continueSide) == continueSide) return false;
				next = getBoard().get(p.add(continueSide.rotateCCW(Rotation.R90)));
				if (next != null && next.getRiver().rotateCW(next.getRotation()).intersect(continueSide) == continueSide) return false;

			}
		}
		return true;
	}

}