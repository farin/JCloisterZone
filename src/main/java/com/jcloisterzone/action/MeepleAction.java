package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.util.Objects;

public class MeepleAction implements SelectFeatureAction {

    private static final long serialVersionUID = 1L;

    //meeple id to set of options
    private final Map<String, Set<FeaturePointer>> options;
    private final Class<? extends Meeple> meepleType;
    private final FeaturePointer origin; // eg wagon source;

    public MeepleAction(Meeple meeple, Set<FeaturePointer> options) {
        this(meeple.getClass(), HashMap.of(meeple.getId(), options), null);
    }

    public MeepleAction(Meeple meeple, Set<FeaturePointer> options, FeaturePointer origin) {
        this(meeple.getClass(), HashMap.of(meeple.getId(), options), origin);
    }

    public MeepleAction(Class<? extends Meeple> meepleType, Map<String, Set<FeaturePointer>> options, FeaturePointer origin) {
        this.options = options;
        this.meepleType = meepleType;
        this.origin = origin;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    public FeaturePointer getOrigin() {
        return origin;
    }

    public boolean isCityOfCarcassoneMove() {
        return origin != null && origin.getLocation().isCityOfCarcassonneQuarter();
    }

    public String getMeepleIdFor(FeaturePointer fp) {
        return options.find(t -> t._2.contains(fp)).get()._1;
    }

    @Override
    public Set<FeaturePointer> getOptions() {
        return options.values().foldLeft(HashSet.empty(), (res, o) -> res.union(o));
    }

    @Override
    public boolean isEmpty() {
        return options.values().foldLeft(true, (res, o) -> res && o.isEmpty());
    }

    public MeepleAction merge(MeepleAction ma) {
        assert ma.meepleType.equals(meepleType);
        assert Objects.equals(ma.origin, origin);
        Map<String, Set<FeaturePointer>> options = this.options;
        for (Tuple2<String, Set<FeaturePointer>> t : ma.options) {
            Set<FeaturePointer> fps = options.get(t._1).getOrElse(HashSet.empty());
            options = options.put(t._1, fps.addAll(t._2));
        }
        return new MeepleAction(meepleType, options, origin);
    }

    public MeepleAction excludeOptions(Set<FeaturePointer> fps) {
        Map<String, Set<FeaturePointer>> options = this.options;
        options = options.map((i,f) -> new Tuple2(i, f.removeAll(fps)));
        return new MeepleAction(meepleType, options, origin);
    }
}
