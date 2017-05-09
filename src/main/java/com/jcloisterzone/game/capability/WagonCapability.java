package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.contentAsLocations;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;

/**
 * @model Queue<Tuple2<Wagon, FeaturePointer>>
 * 	: scored wagons (and unprocessed), order by playing order
 */
public class WagonCapability extends Capability<Queue<Tuple2<Wagon, FeaturePointer>>> {

    //private final Map<Player, Feature> scoredWagons = new HashMap<>();

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, Queue.empty());
    }

    @Override
    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.of((Follower) new Wagon(idProvider.generateId(Wagon.class), player));
    }

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("wagon-move");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            String tileId = tile.getId();
            Map<Location, Feature> features = tile.getInitialFeatures();
            nl = ((Element) nl.item(0)).getElementsByTagName("neighbouring");
            for (int i = 0; i < nl.getLength(); i++) {
                Array<FeaturePointer> fps = contentAsLocations((Element) nl.item(i))
                    .map(l -> new FeaturePointer(Position.ZERO, l))
                    .toArray();

                for (FeaturePointer fp : fps) {
                    Location partLoc = fp.getLocation();
                    Tuple2<Location, Feature> item = features
                        .find(t -> partLoc.isPartOf(t._1))
                        .getOrElseThrow(() -> new IllegalStateException(
                            String.format("%s / <wagon-move>: No feature for %s", tileId, partLoc)
                        ));
                    Completable feature = (Completable) item._2;
                    feature = feature.setNeighboring(fps.remove(fp).toSet());
                    features = features.put(item._1, feature);
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
