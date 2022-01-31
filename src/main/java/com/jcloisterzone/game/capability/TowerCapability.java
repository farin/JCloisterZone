package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

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
        Player player = state.getPlayerActions().getPlayer();

        Set<FeaturePointer> occupiedTowers = state.getDeployedMeeples()
            .filter(t -> t._2.getFeature().equals(Tower.class))
            .map(Tuple2::_2)
            .toSet();

        Set<Tower> openTowers = state.getFeatures(Tower.class).filter(tower -> tower.getPlaces().toSet().intersect(occupiedTowers).isEmpty()).toSet();
        Set<FeaturePointer> openTowersForPiece = openTowers.map(Tower::getPlace);
        Set<FeaturePointer> openTowersForFollower = openTowers.filter(t -> t.getHeight() > 0).map(Tower::getPlace);

        ActionsState as = state.getPlayerActions();

        if (!openTowersForPiece.isEmpty()) {
            if (state.getPlayers().getPlayerTokenCount(player.getIndex(), TowerToken.TOWER_PIECE) > 0) {
                as = as.appendAction(new TowerPieceAction(openTowersForPiece.map(FeaturePointer::getPosition)));
            }
        }

        if (!openTowersForFollower.isEmpty()) {
            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(
                state,
                Vector.of(SmallFollower.class, BigFollower.class, Phantom.class, Ringmaster.class)
            );
            Vector<PlayerAction<?>> actions = availMeeples.map(meeple ->
                new MeepleAction(meeple, openTowersForFollower)
            );
            as = as.appendActions(actions).mergeMeepleActions();
        }

        return state.setPlayerActions(as);
    }
}
