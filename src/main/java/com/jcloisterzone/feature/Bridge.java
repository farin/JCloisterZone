package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileBuilder;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

/*
 * Bridge is used only for initialFeature. When placed on board it's immediately converted to plain Road.
 */
public class Bridge extends Road {

    private static final long serialVersionUID = 1L;

    public Bridge(List<FeaturePointer> places, Set<Edge> openEdges) {
        super(places, openEdges);
    }

    public Bridge(Location bridgeLoc) {
        this(
            List.of(new FeaturePointer(Position.ZERO, bridgeLoc)),
            TileBuilder.initOpenEdges(Stream.ofAll(bridgeLoc.splitToSides()))
        );
    }

    public static String name() {
        return _tr("Bridge");
    }
}
