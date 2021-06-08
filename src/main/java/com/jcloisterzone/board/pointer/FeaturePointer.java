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
    private final Class<? extends Feature> feature;
    private final Location location;

    public FeaturePointer(Position position, Class<? extends Feature> feature, Location location) {
        this.position = position;
        this.feature = feature;
        this.location = location;
    }

    @Override
    public FeaturePointer asFeaturePointer() {
        return this;
    }

    public FeaturePointer translate(Position pos) {
        return new FeaturePointer(position.add(pos), feature, location);
    }

    public FeaturePointer rotateCW(Rotation rot) {
        return new FeaturePointer(position, feature, location.rotateCW(rot));
    }

    public FeaturePointer rotateCCW(Rotation rot) {
        return new FeaturePointer(position, feature, location.rotateCCW(rot));
    }

    public Stream<FeaturePointer> getAdjacent() {
        if (Field.class.isAssignableFrom(feature)) {
            return Stream.ofAll(Location.SIDES)
                .flatMap(loc -> {
                    List<FeaturePointer> res = List.empty();
                    Location l = loc.getLeftField();
                    Location r = loc.getRightField();
                    if (l.intersect(location) != null) {
                        res = res.prepend( new FeaturePointer(position.add(loc), feature, l.rev()));
                    }
                    if (r.intersect(location) != null) {
                        res = res.prepend( new FeaturePointer(position.add(loc), feature, r.rev()));
                    }
                    return res;
                });
        } else {
            return Stream.ofAll(Location.SIDES)
                .filter(loc -> loc.intersect(location) != null)
                .map(loc ->
                    new FeaturePointer(position.add(loc), feature, loc.rev())
                );
        }
    }

    public boolean isPartOf(FeaturePointer other) {
        return position.equals(other.position) && feature.equals(other.feature) && location.isPartOf(other.location);
    }

    public Position getPosition() {
        return position;
    }

    public FeaturePointer setPosition(Position position) {
        if (this.position == position) return this;
        return new FeaturePointer(position, feature, location);
    }

    public Class<? extends Feature> getFeature() {
        return feature;
    }

    public FeaturePointer setFeature(Class<? extends Feature> feature) {
        if (this.feature == feature) return this;
        return new FeaturePointer(position, feature, location);
    }

    public Location getLocation() {
        return location;
    }

    public FeaturePointer setLocation(Location location) {
        if (this.location == location) return this;
        return new FeaturePointer(position, feature, location);
    }

    @Override
    public String toString() {
        return String.format("{%s,%s,%s}", position, feature.getSimpleName(), location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, feature, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FeaturePointer other = (FeaturePointer) obj;
        if (!Objects.equals(location, other.location)) return false;
        if (!Objects.equals(feature, other.feature)) return false;
        if (!Objects.equals(position, other.position)) return false;
        return true;
    }
}
