package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;


public class RiverCapability extends Capability {

    private static final String R1_LAKE_ID = "R1.I.e";
    private static final String R2_LAKE_ID = "R2.I.v";
    private static final String R2_FORK_ID = "R2.III";

    private static List<String> STREAM_IDS =  Arrays.asList("R1.I.s", "R2.I.s", "GQ.RFI");

    public RiverCapability(Game game) {
        super(game);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl;
        nl = xml.getElementsByTagName("river");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            Location river = XMLUtils.union(XMLUtils.asLocation((Element) nl.item(0)));
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
        getTilePack().setGroupState("default", TileGroupState.WAITING);
        getTilePack().setGroupState("river-start", TileGroupState.ACTIVE);
        if (!game.hasExpansion(Expansion.RIVER_II)) {
            getTilePack().setGroupState("river", TileGroupState.ACTIVE);
        }
    }

    public void activateNonRiverTiles() {
        getTilePack().setGroupState("default", TileGroupState.ACTIVE);
        getTilePack().setGroupState("river", TileGroupState.RETIRED);
        Tile lake = getTilePack().drawTile(TilePack.INACTIVE_GROUP, getLakeId());
        getBoard().refreshAvailablePlacements(lake);
        if (!getBoard().getAvailablePlacements().isEmpty()) {
	        Entry<Position, Set<Rotation>> entry = getBoard().getAvailablePlacements().entrySet().iterator().next();
	        lake.setRotation(entry.getValue().iterator().next());
	        getBoard().add(lake, entry.getKey());
	        getBoard().mergeFeatures(lake);
	        game.post(new TileEvent(TileEvent.PLACEMENT, null, lake, lake.getPosition()));
        }
    }

    @Override
    public void turnPartCleanUp() {
        if (getCurrentTile().getRiver() == null) return;
        if (getTilePack().isEmpty()) {
            if (getTilePack().getGroupState("river") == TileGroupState.ACTIVE) {
                activateNonRiverTiles();
            } else {
                getTilePack().setGroupState("river-start", TileGroupState.RETIRED);
                getTilePack().setGroupState("river", TileGroupState.ACTIVE);
            }
        }
    }

    private Location getTileRiver(Tile tile) {
    	 return tile.getRiver().rotateCW(tile.getRotation());
    }


    enum FollowResult {
    	LEGAL_WITH_TILE,
    	LEGAL,
    	ILLEGAL
    }

    //direction is relative direction ^ < or >
    private FollowResult followPath(Tile riverTile, Position riverPos, Location forward, char direction, Tile checkTile, Position checkTilePos) {
    	boolean checkTilePartOfRiver = false;


    	while (forward != null) {
    		riverPos = riverPos.add(forward);
    		if (riverPos.equals(checkTilePos)) {
    			riverTile = checkTile;
    			checkTilePartOfRiver = true;
    		} else {
    			riverTile = getBoard().get(riverPos);
    		}
    		if (riverTile == null) {
    			if (getBoard().get(riverPos.add(forward)) != null) return FollowResult.ILLEGAL; //too few space
    			break;
    		}
    		Location prev = forward;
    		Location riverLoc = getTileRiver(riverTile);
    		if (!prev.rev().isPartOf(riverLoc)) return FollowResult.ILLEGAL; //river is not continuous;
    		forward = riverLoc.substract(prev.rev());
    		if (riverTile.getId().equals(R2_FORK_ID)) {
    			for (Location part : forward.splitToSides()) {
    				char branchDir = '^';
    				for (Location turn : riverLoc.splitToSides()) {
    					if (turn == part) continue;
    					if (turn.prev() == part) branchDir = branchDir == '<' ? '!' : '>';
    					if (turn.next() == part) branchDir = branchDir == '>' ? '!' : '<';
    				}
					FollowResult branchResult = followPath(riverTile, riverPos, part, branchDir, checkTile, checkTilePos);
					if (branchResult == FollowResult.ILLEGAL) return FollowResult.ILLEGAL;
					if (branchResult == FollowResult.LEGAL_WITH_TILE) checkTilePartOfRiver = true;
    			}
    			break;
    		} else {
	    		if (prev == forward) {
	    			direction = '^';
	    		} else if (prev.next() == forward) {
	    			if (direction == '>' || direction == '!') return FollowResult.ILLEGAL; //U-turn
	    			direction = '>';
	    		} else if (prev.prev() == forward) {
	    			if (direction == '<' || direction == '!') return FollowResult.ILLEGAL; //U-turn
	    			direction = '<';
	    		}
    		}
    	}

    	return checkTilePartOfRiver ? FollowResult.LEGAL_WITH_TILE : FollowResult.LEGAL;
    }

    @Override
    public boolean isTilePlacementAllowed(Tile checkTile, Position checkTilePos) {
    	if (checkTile.getRiver() == null) return true;

    	//find stream to start from
    	for (Tile t : getBoard().getAllTiles()) {
    		if (STREAM_IDS.contains(t.getId())) {
    			return followPath(t, t.getPosition(), getTileRiver(t), '^', checkTile, checkTilePos) == FollowResult.LEGAL_WITH_TILE;
    		}
    	}

    	return true;
    }

}