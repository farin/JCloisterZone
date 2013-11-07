package com.jcloisterzone.game.capability;

import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;


public class RiverCapability extends Capability {

    private static final String R1_LAKE_ID = "R1.I.e";
    private static final String R2_LAKE_ID = "R2.I.v";

    public RiverCapability(Game game) {
        super(game);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl;
        nl = xml.getElementsByTagName("river");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            Location river = XmlUtils.union(XmlUtils.asLocation((Element) nl.item(0)));
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

    private String getLakeId() {
        return game.hasExpansion(Expansion.RIVER_II) ? R2_LAKE_ID : R1_LAKE_ID;
    }

    @Override
    public void begin() {
        getTilePack().deactivateGroup("default");
        getTilePack().activateGroup("river-start");
        if (!game.hasExpansion(Expansion.RIVER_II)) {
            getTilePack().activateGroup("river");
        }
    }

    public void activateNonRiverTiles() {
        getTilePack().activateGroup("default");
        getTilePack().deactivateGroup("river");
        Tile lake = getTilePack().drawTile(TilePack.INACTIVE_GROUP, getLakeId());
        getBoard().refreshAvailablePlacements(lake);
        Entry<Position, Set<Rotation>> entry = getBoard().getAvailablePlacements().entrySet().iterator().next();
        lake.setRotation(entry.getValue().iterator().next());
        getBoard().add(lake, entry.getKey());
        getBoard().mergeFeatures(lake);
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
    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        if (tile.getRiver() == null) return true;
        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {

            //check river connection
            Location tileRelativePosition = e.getKey();
            Tile placedTile = e.getValue();
            if (placedTile.getRiver() == null) return false; //e.g. count of carcassone preplaced tiles
            boolean r1 = tileRelativePosition.rotateCCW(tile.getRotation()).isPartOf(tile.getRiver());
            boolean r2 = tileRelativePosition.rotateCCW(placedTile.getRotation()).rev().isPartOf(placedTile.getRiver());
            if (!(r1 & r2)) return false;

            //check U-turn
            Location continueRiver = tile.getRiver().rotateCW(tile.getRotation()).substract(tileRelativePosition);
            if (continueRiver == Location.INNER_FARM) return true; //lake
            for (Location continueSide: Location.sides()) { //split beacuse of river fork
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