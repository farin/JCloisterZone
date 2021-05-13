package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MoveDragonAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

public class DragonMovePhase extends Phase {

    public DragonMovePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    private Vector<Position> getVisitedPositions(GameState state) {
        Vector<Position> visited = state.getCapabilityModel(DragonCapability.class);
        return visited == null ? Vector.empty() : visited;
    }

    @Override
    public StepResult enter(GameState state) {
        Vector<Position> visited = getVisitedPositions(state);

        if (visited.size() == DragonCapability.DRAGON_MOVES) {
            return next(endDragonMove(state));
        }

        Set<Position> availMoves =  getAvailDragonMoves(state, visited);
        if (availMoves.isEmpty()) {
            return next(endDragonMove(state));
        }

        Dragon dragon = state.getNeutralFigures().getDragon();
        Player p = state.getTurnPlayer();
        p = state.getPlayers().getPlayer((p.getIndex() + visited.length()) % state.getPlayers().getPlayers().length());

        return promote(state.setPlayerActions(
            new ActionsState(p, new MoveDragonAction(dragon.getId(), availMoves), false)
        ));
    }

    private GameState endDragonMove(GameState state) {
        state = state.setCapabilityModel(DragonCapability.class, Vector.empty());
        state = clearActions(state);
        return state;
    }


    public Set<Position> getAvailDragonMoves(GameState state, Vector<Position> visited) {
        Set<Position> result = HashSet.empty();
        BoardPointer fairyPtr = state.getNeutralFigures().getFairyDeployment();
        Position fairyPosition = fairyPtr == null ? null : fairyPtr.getPosition();
        Position dragonPosition = state.getNeutralFigures().getDragonDeployment();

        for (Position offset: Position.ADJACENT.values()) {
            Position pos = dragonPosition.add(offset);
            PlacedTile pt = state.getPlacedTile(pos);

            if (pt == null || CountCapability.isTileForbidden(pt.getTile())) continue;
            if (visited.contains(pos)) continue;
            if (pos.equals(fairyPosition)) continue;

            result = result.add(pos);
        }
        return result;
    }

    @PhaseMessageHandler
    public StepResult handleMoveNeutralFigure(GameState state, MoveNeutralFigureMessage msg) {
        BoardPointer ptr = msg.getTo();
        NeutralFigure<?> fig = state.getNeutralFigures().getById(msg.getFigureId());

        if (!(fig instanceof Dragon)) {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }

        Vector<Position> visited = getVisitedPositions(state);
        Set<Position> availMoves =  getAvailDragonMoves(state, visited);

        Position pos = ptr.getPosition();
        if (!availMoves.contains(pos)) {
            throw new IllegalArgumentException("Invalid dragon move.");
        }

        Position dragonPosition = state.getNeutralFigures().getDragonDeployment();

        state = (
            new MoveNeutralFigure<>((Dragon) fig, pos, state.getActivePlayer())
        ).apply(state);
        state = state.mapCapabilityModel(DragonCapability.class, moves -> moves.append(dragonPosition));

        for (Tuple2<Meeple, FeaturePointer> t: state.getDeployedMeeples()) {
            Meeple m = t._1;
            FeaturePointer fp = t._2;
            if (pos.equals(fp.getPosition()) && m.canBeEatenByDragon(state)) {
                state = (new UndeployMeeple(m, true)).apply(state);
            }
        }

        return enter(state);
    }
}
