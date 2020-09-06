package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class MeepleAction implements SelectFeatureAction {

    private static final long serialVersionUID = 1L;

    //meeple id to set of options
    private final Map<String, Set<FeaturePointer>> options;
    private final Class<? extends Meeple> meepleType;
    private final boolean cityOfCarcassoneMove;

    public MeepleAction(Meeple meeple, Set<FeaturePointer> options) {
        this(meeple.getClass(), HashMap.of(meeple.getId(), options), false);
    }

    public MeepleAction(Meeple meeple, Set<FeaturePointer> options, boolean cityOfCarcassoneMove) {
        this(meeple.getClass(), HashMap.of(meeple.getId(), options), cityOfCarcassoneMove);
    }

    public MeepleAction(Class<? extends Meeple> meepleType, Map<String, Set<FeaturePointer>> options, boolean cityOfCarcassoneMove) {
        this.options = options;
        this.meepleType = meepleType;
        this.cityOfCarcassoneMove = cityOfCarcassoneMove;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    public boolean isCityOfCarcassoneMove() {
        return cityOfCarcassoneMove;
    }

    public String getMeepleIdFor(FeaturePointer fp) {
        return options.find(t -> t._2.contains(fp)).get()._1;
    }

    @Override
    public Message select(FeaturePointer fp) {
        return new DeployMeepleMessage(fp, getMeepleIdFor(fp), null);
    }

    @Override
    public Set<FeaturePointer> getOptions() {
        return options.values().foldLeft(HashSet.empty(), (res, o) -> res.union(o));
    }

    @Override
    public boolean isEmpty() {
        return options.values().foldLeft(true, (res, o) -> res && o.isEmpty());
    }

    @Override
    public String toString() {
        return "place " + meepleType.getSimpleName();
    }

    public MeepleAction merge(MeepleAction ma) {
        assert ma.meepleType.equals(meepleType);
        assert ma.cityOfCarcassoneMove == cityOfCarcassoneMove;
        Map<String, Set<FeaturePointer>> options = this.options;
        for (Tuple2<String, Set<FeaturePointer>> t : ma.options) {
            Set<FeaturePointer> fps = options.get(t._1).getOrElse(HashSet.empty());
            options = options.put(t._1, fps.addAll(t._2));
        }
        return new MeepleAction(meepleType, options, cityOfCarcassoneMove);
    }

}
