package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PointsExpression;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.VineyardCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.Tuple2;
import io.vavr.collection.*;

/**
 * Cloister or Shrine
 */
public class Garden extends TileFeature implements Scoreable, CloisterLike {

    private static final long serialVersionUID = 1L;
    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.CLOISTER));

    protected final Set<FeaturePointer> neighboring; //for wagon move

    public Garden() {
        this(INITIAL_PLACE, HashSet.empty());
    }

    public Garden(List<FeaturePointer> places, Set<FeaturePointer> neighboring) {
        super(places);
        this.neighboring = neighboring;
    }

    @Override
    public Garden setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Garden(places, neighboring);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Garden(placeOnBoardPlaces(pos, rot), placeOnBoardNeighboring(pos, rot));
    }


    @Override
    public PointsExpression getPoints(GameState state) {
        Position p = places.get().getPosition();
        int adjacent = state.getAdjacentAndDiagonalTiles2(p).size();
        Map<String, Integer> args = HashMap.of("tiles", adjacent + 1);
        int points = adjacent + 1;
        return new PointsExpression(points, adjacent == 8 ? "garden" : "gardeb.incomplete", args).merge(getLittleBuildingPoints(state));
    }

    public static String name() {
        return "Garden";
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
