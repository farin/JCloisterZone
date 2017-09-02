package com.jcloisterzone.feature;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public abstract class TileStructure extends TileFeature implements Structure {

    public TileStructure(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public Stream<Tuple2<Meeple, FeaturePointer>> getMeeples2(GameState state) {
        Set<FeaturePointer> fps = HashSet.ofAll(places);
        return Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> fps.contains(t._2));
    }


}
