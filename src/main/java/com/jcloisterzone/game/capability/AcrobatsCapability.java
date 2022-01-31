package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.ScoreAcrobatsAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.Acrobats;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.UndeployMeeples;
import io.vavr.collection.*;

public class AcrobatsCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	private static final int FULL_ACROBATS = 3;

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();

        Player active = state.getActivePlayer();

        PlacedTile lastPlaced = state.getLastPlaced();
        Position currentTilePos = lastPlaced.getPosition();
        
        Stream<Acrobats> acrobats = state.getFeatures(Acrobats.class);
        SmallFollower meeple = (SmallFollower) active.getMeeplesFromSupply(state, Vector.of(SmallFollower.class)).getOrNull();

        // Not allow to place Acrobat on tile with Bridge
        Set<Position> placedBridges = state.hasCapability(BridgeCapability.class) ? state.getCapabilityModel(BridgeCapability.class).map(FeaturePointer::getPosition) : HashSet.empty();
        
        // When Magic Portal, allow place also to all acrobats spaces
        boolean hasMagicPortal = lastPlaced.getTile().hasModifier(PortalCapability.MAGIC_PORTAL);

        Set<FeaturePointer> acrobatsToScore = HashSet.empty();

        for (Acrobats feature : acrobats) {
            int meeplesCount = feature.getMeeples(state).length();
            FeaturePointer fp = feature.getPlace();
            if (placedBridges.contains(fp.getPosition())) continue;

            if (meeplesCount >= FULL_ACROBATS) {
                acrobatsToScore = acrobatsToScore.add(fp);
                continue;
            }

            if (meeple != null) {
                Position pos = fp.getPosition();
                boolean canPlace = hasMagicPortal || Math.abs(currentTilePos.x - pos.x) <= 1 && Math.abs(currentTilePos.y - pos.y) <= 1;
                if (canPlace) {
                    actions = actions.appendAction(new MeepleAction(meeple, HashSet.of(fp)));
                }
            }
        }

        if (!acrobatsToScore.isEmpty()) {
            actions = actions.appendAction(new ScoreAcrobatsAction(acrobatsToScore));
        }

        if (state.getPlayerActions() != actions) {
            state = state.setPlayerActions(actions.mergeMeepleActions());
        }

        return state;
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        for (Acrobats acrobats : state.getFeatures(Acrobats.class)) {
            if (acrobats.isOccupied(state)) {
        	    state = scoreAcrobats(state, acrobats, false);
            }
        }
        return state;
    }
    
    public GameState scoreAcrobats(GameState state, Acrobats acrobats, boolean undeployMeeples) {
        List<ScoreEvent.ReceivedPoints> points = List.empty();
        for (var t : acrobats.getMeeples(state).groupBy(Meeple::getPlayer)) {
            int meepleCount = t._2.size();
            ExprItem expr = new ExprItem(meepleCount, "meeples", 5 * meepleCount);
            points = points.append(new ScoreEvent.ReceivedPoints(new PointsExpression("acrobats", expr), t._1, acrobats.getPlace().getPosition()));
        }
        state = (new AddPoints(points, false)).apply(state);
        if (undeployMeeples) {
            state = (new UndeployMeeples(acrobats, false)).apply(state);
        }
    	return state;
    }
}
