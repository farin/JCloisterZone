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
    public GameState onTurnPartCleanUp(GameState state) {
        return setModel(state, Queue.empty());
    }
}
