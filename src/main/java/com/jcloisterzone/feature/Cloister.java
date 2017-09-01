package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;


public class Cloister extends ScoreableFeature implements Completable {

    private static final long serialVersionUID = 1L;

    protected final Set<FeaturePointer> neighboring; //for wagon move

    protected final boolean shrine;
    protected final boolean monastery;
    protected final boolean yagaHut;

    public Cloister(List<FeaturePointer> places) {
        this(places, HashSet.empty(), false, false, false);
    }

    public Cloister(List<FeaturePointer> places, Set<FeaturePointer> neighboring, boolean shrine, boolean monastery, boolean yagaHut) {
        super(places);
        this.neighboring = neighboring;
        this.shrine = shrine;
        this.monastery = monastery;
        this.yagaHut = yagaHut;
    }

    @Override
    public Cloister setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Cloister(places, neighboring, shrine, monastery, yagaHut);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Cloister(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardNeighboring(pos, rot),
            shrine, monastery, yagaHut
        );
    }

    public boolean isShrine() {
        return shrine;
    }

    public Cloister setShrine(boolean shrine) {
        if (this.shrine == shrine) return this;
        return new Cloister(places, neighboring, shrine, monastery, yagaHut);
    }

    public boolean isMonastery() {
        return monastery;
    }

    public Cloister setMonastery(boolean monastery) {
        if (this.monastery == monastery) return this;
        return new Cloister(places, neighboring, shrine, monastery, yagaHut);
    }

    public boolean isYagaHut() {
        return yagaHut;
    }

    public Cloister setYagaHut(boolean yagaHut) {
        if (this.yagaHut == yagaHut) return this;
        return new Cloister(places, neighboring, shrine, monastery, yagaHut);
    }

    @Override
    public boolean isOpen(GameState state) {
        Position p = places.get().getPosition();
        return state.getAdjacentAndDiagonalTiles2(p).size() < 8;
    }

    @Override
    public int getPoints(GameState state) {
        Position p = places.get().getPosition();
        return state.getAdjacentAndDiagonalTiles2(p).size() + 1;
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
        return _("Cloister");
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
