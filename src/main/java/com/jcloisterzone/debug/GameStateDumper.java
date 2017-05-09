package com.jcloisterzone.debug;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;
import io.vavr.collection.Seq;

public class GameStateDumper {

    public static void dumpFeatures(GameState state) {
        System.out.println("----- features < " + System.identityHashCode(state));
        Seq<Feature> features = state.getFeatureMap()
            .values().distinct()
            .sortBy(f -> f.getClass().getSimpleName());

        for (Feature f : features) {
            System.out.println(f.toString() + " (" + System.identityHashCode(f) + ")");
            List<FeaturePointer> places = state.getFeatureMap()
                .filter((fp, _f) -> f == _f)
                .keySet().toList().sortBy(fp -> fp.getPosition());
            System.out.println(" - places:");
            for (FeaturePointer fp : places) {
                System.out.println("   - " + fp.toString());
            }
            if (f instanceof CompletableFeature) {
                System.out.println(" - open edges:");
                for (Edge edge : ((CompletableFeature<?>)f).getOpenEdges()) {
                    System.out.println("   - " + edge.toString());
                }
            }
        }
        System.out.println("--------------------");
    }

//    public static void dumpFeaturesByGropuTile(GameState state) {
//        state.getFeatures().groupBy(t -> t._1.getPosition());
//
//    }
}
