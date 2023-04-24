package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.MoveDragonAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.BlackDragon;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.capability.BlackDragonCapabilityModel;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.capability.BlackDragonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

public class BlackDragonMovePhase extends Phase {

    public BlackDragonMovePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        BlackDragonCapabilityModel model = state.getCapabilityModel(BlackDragonCapability.class);
        if (model.getVisited().size() == model.getMoves()) {
            return next(endBlackDragonMove(state));
        }
        Set<Position> availMoves =  getAvailBlackDragonMoves(state, model.getVisited());
        if (availMoves.isEmpty()) {
            return next(endBlackDragonMove(state));
        }
        BlackDragon blackdragon = state.getNeutralFigures().getBlackDragon();
        return promote(state.setPlayerActions(
            new ActionsState(state.getTurnPlayer(), new MoveDragonAction(blackdragon.getId(), availMoves), false)
        ));
    }

    private GameState endBlackDragonMove(GameState state) {
    	state = state.addFlag(Flag.BLACK_DRAGON_MOVED);
        state = state.setCapabilityModel(BlackDragonCapability.class, new BlackDragonCapabilityModel(BlackDragonCapability.EMPTY_VISITED, 0));
        state = clearActions(state);
        return state;
    }

    public Set<Position> getAvailBlackDragonMoves(GameState state, Vector<Position> visited) {
        Set<Position> result = HashSet.empty();
        Position blackdragonPosition = state.getNeutralFigures().getBlackDragonDeployment();

        if (blackdragonPosition != null) {
	        for (Position offset: Position.ADJACENT.values()) {
	            Position pos = blackdragonPosition.add(offset);
	            PlacedTile pt = state.getPlacedTile(pos);
	
	            if (pt == null || CountCapability.isTileForbidden(pt.getTile())) continue;
	            if (visited.contains(pos)) continue;
	
	            result = result.add(pos);
	        }
        }
        return result;
    }

    @PhaseMessageHandler
    public StepResult handleMoveNeutralFigure(GameState state, MoveNeutralFigureMessage msg) {
        BoardPointer ptr = msg.getTo();
        NeutralFigure<?> fig = state.getNeutralFigures().getById(msg.getFigureId());

        if (!(fig instanceof BlackDragon)) {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }

        BlackDragonCapability cap = state.getCapabilities().get(BlackDragonCapability.class);
        BlackDragonCapabilityModel model = state.getCapabilityModel(BlackDragonCapability.class);

        Vector<Position> visited = model.getVisited();
        Set<Position> availMoves =  getAvailBlackDragonMoves(state, visited);

        final Position pos = ptr.getPosition();
        if (!availMoves.contains(pos)) {
            throw new IllegalArgumentException("Invalid black dragon move.");
        }

        state = (
            new MoveNeutralFigure<>((BlackDragon) fig, pos, state.getActivePlayer())
        ).apply(state);

        state = state.mapCapabilityModel(BlackDragonCapability.class, m -> {
            return m.setVisited(m.getVisited().append(pos));
        });

        state = cap.clearTile(state, pos);
        return enter(state);
    }
}
