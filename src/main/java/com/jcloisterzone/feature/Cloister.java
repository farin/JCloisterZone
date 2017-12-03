package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

/**
 * Cloister or Shrine
 */
public class Cloister extends TileFeature implements Scoreable, CloisterLike {

    private static final long serialVersionUID = 1L;
    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.CLOISTER));

    protected final Set<FeaturePointer> neighboring; //for wagon move

    protected final boolean shrine;
    protected final boolean monastery;

    public Cloister() {
        this(INITIAL_PLACE, HashSet.empty(), false, false);
    }

    public Cloister(List<FeaturePointer> places, Set<FeaturePointer> neighboring, boolean shrine, boolean monastery) {
        super(places);
        this.neighboring = neighboring;
        this.shrine = shrine;
        this.monastery = monastery;
    }

    @Override
    public Cloister setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Cloister(places, neighboring, shrine, monastery);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Cloister(placeOnBoardPlaces(pos, rot), placeOnBoardNeighboring(pos, rot), shrine, monastery);
    }

    public boolean isShrine() {
        return shrine;
    }

    public Cloister setShrine(boolean shrine) {
        if (this.shrine == shrine) return this;
        return new Cloister(places, neighboring, shrine, monastery);
    }

    public boolean isMonastery() {
        return monastery;
    }

    public Cloister setMonastery(boolean monastery) {
        if (this.monastery == monastery) return this;
        return new Cloister(places, neighboring, shrine, monastery);
    }

    @Override
    public int getPoints(GameState state) {
        Position p = places.get().getPosition();
        return state.getAdjacentAndDiagonalTiles2(p).size() + 1 + getLittleBuildingPoints(state);
    }

    @Override
    public Set<Position> getTilePositions() {
        return HashSet.of(places.get().getPosition());
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CLOISTER;
    }

    public static String name() {
        return _tr("Cloister");
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
