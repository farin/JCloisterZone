package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.jcloisterzone.XMLUtils.contentAsLocations;

/**
 * @model Queue<Tuple2<Wagon, FeaturePointer>>
 *  : scored wagons (and unprocessed), order by playing order
 */
public class WagonCapability extends Capability<Queue<Tuple2<Wagon, FeaturePointer>>> {

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, Queue.empty());
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        for (Element moveEl : XMLUtils.getElementStreamByTagName(tileElements, "wagon-move")) {
            String tileId = tile.getId();
            Map<FeaturePointer, Feature> features = tile.getInitialFeatures();
            NodeList nl = moveEl.getElementsByTagName("neighbouring");
            for (int i = 0; i < nl.getLength(); i++) {
                Array<Location> locations = contentAsLocations((Element) nl.item(i)).toArray();

                // convert from part location to exact location
                Map<FeaturePointer, Feature> _features = features;
                Array<FeaturePointer> fps = locations.map(partLoc -> {
                    // Location partLoc = fp.getLocation();
                    Tuple2<FeaturePointer, Feature> item = _features
                        .find(t -> partLoc.isPartOf(t._1.getLocation()))
                        .getOrElseThrow(() -> new IllegalStateException(
                            String.format("%s / <wagon-move>: No feature for %s", tileId, partLoc)
                        ));
                    return item._1;
                });

                for (FeaturePointer fp : fps) {
                    Completable feature = (Completable) features.get(fp).getOrNull();
                    feature = feature.setNeighboring(feature.getNeighboring().addAll(fps.remove(fp)));
                    features = features.put(fp, feature);
                }
            }
            tile = tile.setInitialFeatures(features);
        }
        return tile;
    }


    @Override
    public GameState onTurnPartCleanUp(GameState state) {
        return setModel(state, Queue.empty());
    }
}
