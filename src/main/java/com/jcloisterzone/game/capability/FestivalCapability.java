package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public class FestivalCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    public final String UNDEPLOY_FESTIVAL = "festival";


    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "festival").isEmpty()) {
            tile = tile.setTileTrigger(TileTrigger.FESTIVAL);
        }
        return tile;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        if (placedTile.getTile().getTrigger() != TileTrigger.FESTIVAL) {
            return state;
        }

        Player player = state.getTurnPlayer();

        Stream<Tuple2<Meeple, FeaturePointer>> meeples = Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> t._1.getPlayer().equals(player));

        if (state.getBooleanValue(Rule.FESTIVAL_FOLLOWER_ONLY)) {
            meeples = meeples.filter(Predicates.instanceOf(Follower.class));
        }

        Set<MeeplePointer> options = meeples.map(MeeplePointer::new).toSet();
        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new ReturnMeepleAction(options, ReturnMeepleSource.FESTIVAL));
    }
}
