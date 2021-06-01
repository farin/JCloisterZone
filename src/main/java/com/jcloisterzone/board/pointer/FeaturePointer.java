package com.jcloisterzone.board.pointer;

import java.util.Objects;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Feature;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

@Immutable
public class FeaturePointer implements BoardPointer {

    private static final long serialVersionUID = 1L;

    private final Position position;
    private final Location location;

    public FeaturePointer(Position position, Location location) {
        this.position = position;
        this.location = location;
    }

    @Override
    public FeaturePointer asFeaturePointer() {
        return this;
    }

    public FeaturePointer translate(Position pos) {
        return new FeaturePointer(position.add(pos), location);
    }

    public FeaturePointer rotateCW(Rotation rot) {
        return new FeaturePointer(position, location.rotateCW(rot));
    }

    public FeaturePointer rotateCCW(Rotation rot) {
        return new FeaturePointer(position, location.rotateCCW(rot));
    }

    public Stream<FeaturePointer> getAdjacent(Class<? extends Feature> forType) {
        boolean isField = Field.class.isAssignableFrom(forType);

        if (isField) {
            return Stream.ofAll(Location.SIDES)
                .flatMap(loc -> {
                    List<FeaturePointer> res = List.empty();
                    Location l = loc.getLeftField();
                    Location r = loc.getRightField();
                    if (l.intersect(location) != null) {
                        res = res.prepend( new FeaturePointer(position.add(loc), l.rev()));
                    }
                    if (r.intersect(location) != null) {
                        res = res.prepend( new FeaturePointer(position.add(loc), r.rev()));
                    }
                    return res;
                });
        } else {
            return Stream.ofAll(Location.SIDES)
                .filter(loc -> loc.intersect(location) != null)
                .map(loc ->
                    new FeaturePointer(position.add(loc), loc.rev())
                );
        }
    }

    public boolean isPartOf(FeaturePointer other) {
        return position.equals(other.position) && location.isPartOf(other.location);
    }

    public Position getPosition() {
        return position;
    }

    public FeaturePointer setPosition(Position position) {
        if (this.position == position) return this;
        return new FeaturePointer(position, location);
    }

    public Location getLocation() {
        return location;
    }

    public FeaturePointer setLocation(Location location) {
        if (this.location == location) return this;
        return new FeaturePointer(position, location);
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", position, location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FeaturePointer other = (FeaturePointer) obj;
        if (!Objects.equals(location, other.location)) return false;
        if (!Objects.equals(position, other.position)) return false;
        return true;
    }
}
