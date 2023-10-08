package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class Castle extends TileFeature implements Scoreable {

    private static final long serialVersionUID = 1L;

    public Castle(List<FeaturePointer> places) {
        super(places);
        assert places.size() == 2 && places.get(0).getFeature().equals(Castle.class);
    }

    public Edge getEdge() {
        return new Edge(places.get(0).getPosition(), places.get(1).getPosition());
    }

    public Set<Position> getTilePositions() {
        return HashSet.ofAll(places.map(FeaturePointer::getPosition));
    }

    public Set<Position> getVicinity() {
        Position p0 = places.get(0).getPosition();
        Position p1 = places.get(1).getPosition();
        Set<Position> vicinity = HashSet.of(p0, p1);
        if (p0.x == p1.x) {
            vicinity = vicinity.addAll(List.of(
                p0.add(Location.W),
                p0.add(Location.E),
                p1.add(Location.W),
                p1.add(Location.E)
            ));
        } else {
            vicinity = vicinity.addAll(List.of(
                p0.add(Location.N),
                p0.add(Location.S),
                p1.add(Location.N),
                p1.add(Location.S)
            ));
        }
        assert vicinity.size() == 6;
        return vicinity;
    }

    public static String name() {
        return "Castle";
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        throw new UnsupportedOperationException();
    }
}
