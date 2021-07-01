package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.AbbeyEdge;
import com.jcloisterzone.feature.Monastery;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;


/**
 * @model Integer: store playerIndex during final abbey placement turn
 */
public class AbbeyCapability extends Capability<Integer> {

	public enum AbbeyToken implements Token {
		ABBEY_TILE // represent Abbey tile in player's supply
	}

	private static final long serialVersionUID = 1L;

    /** The constant ABBEY_TILE_ID. */
    public static final String ABBEY_TILE_ID = "AM/A";
    /** Abbey tile, not placed yet. */
    public static Tile ABBEY_TILE;

    static {
        HashMap<FeaturePointer, Feature> features = io.vavr.collection.HashMap.of(
            new FeaturePointer(Position.ZERO, Monastery.class, Location.I), new Monastery(HashMap.empty())
        );
        for (Location side : Location.SIDES) {
            FeaturePointer fp = new FeaturePointer(Position.ZERO, AbbeyEdge.class, side);
            features = features.put(fp, new AbbeyEdge(List.of(fp)));
        }

        ABBEY_TILE = new Tile(ABBEY_TILE_ID, features, HashSet.empty());
    }

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(AbbeyToken.ABBEY_TILE, 1));
    }

    /**
     * Checks if {@code tile} is an abbey.
     *
     * @return {@code true} if {@code tile} is an abbey, {@code false} otherwise
     */
    public static boolean isAbbey(Tile tile) {
        return tile.getId().equals(ABBEY_TILE_ID);
    }
}
