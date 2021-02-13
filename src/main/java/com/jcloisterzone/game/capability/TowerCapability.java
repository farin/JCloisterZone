package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.*;

/**
 * @model Array<List<String>> - list of captured meeples for each players
 */
public final class TowerCapability extends Capability<Array<List<Follower>>> {

	public enum TowerToken implements Token {
		TOWER_PIECE
    }

	private static final long serialVersionUID = 1L;

    public static final int RANSOM_POINTS = 3;

    private int getInitialPiecesCount(GameState state) {
        switch (state.getPlayers().getPlayers().length()) {
        case 1:
        case 2: return 10;
        case 3: return 9;
        case 4: return 7;
        case 5: return 6;
        case 6: return 5;
        case 7: return 4;
        default: return 3;
        }
    }

    @Override
    public GameState onStartGame(GameState state) {
        int pieces = getInitialPiecesCount(state);
        state = state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(TowerToken.TOWER_PIECE, pieces));
        state = setModel(state, Array.fill(state.getPlayers().length(), List::empty));
        return state;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        if (state.hasFlag(Flag.TOWER_INCREASED)) {
            return state;
        }

        Player player = state.getPlayerActions().getPlayer();

        Set<FeaturePointer> occupiedTowers = state.getDeployedMeeples()
            .filter(t -> t._2.getLocation().equals(Location.TOWER))
            .map(Tuple2::_2)
            .toSet();

        Stream<Tuple2<FeaturePointer, Feature>> openTowersStream = Stream.ofAll(state.getFeatureMap())
            .filter(t -> (t._2 instanceof Tower))
            .filter(t -> !occupiedTowers.contains(t._1));

        Set<FeaturePointer> openTowersForPiece = openTowersStream
            .map(Tuple2::_1).toSet();

        Set<FeaturePointer> openTowersForFollower = openTowersStream
            .filter(t -> ((Tower)t._2).getHeight() > 0)
            .map(Tuple2::_1).toSet();

        ActionsState as = state.getPlayerActions();

        if (!openTowersForPiece.isEmpty()) {
            if (state.getPlayers().getPlayerTokenCount(player.getIndex(), TowerToken.TOWER_PIECE) > 0) {
                as = as.appendAction(new TowerPieceAction(openTowersForPiece.map(FeaturePointer::getPosition)));
            }
        }

        if (!openTowersForFollower.isEmpty()) {
            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(
                state,
                Vector.of(SmallFollower.class, BigFollower.class, Phantom.class)
            );
            Vector<PlayerAction<?>> actions = availMeeples.map(meeple ->
                new MeepleAction(meeple, openTowersForFollower)
            );
            as = as.appendActions(actions).mergeMeepleActions();
        }

        return state.setPlayerActions(as);
    }
}
