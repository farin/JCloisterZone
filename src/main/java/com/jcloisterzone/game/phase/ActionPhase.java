package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Shepherd;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability.BrigeToken;
import com.jcloisterzone.game.capability.FestivalCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability.TowerToken;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.reducers.PlaceBridge;
import com.jcloisterzone.reducers.PlaceLittleBuilding;
import com.jcloisterzone.reducers.PlaceTunnel;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;

import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.Predicates;
import io.vavr.collection.Vector;


public class ActionPhase extends AbstractActionPhase {

    public ActionPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();

        Vector<Class<? extends Meeple>> meepleTypes = Vector.of(
            SmallFollower.class, BigFollower.class, Phantom.class,
            Wagon.class, Mayor.class, Builder.class, Pig.class, Shepherd.class
        );

        Vector<PlayerAction<?>> actions = prepareMeepleActions(state, meepleTypes);

        GameState nextState = state.setPlayerActions(
            new ActionsState(player, actions, true)
        );

        for (Capability<?> cap : nextState.getCapabilities().toSeq()) {
            nextState = cap.onActionPhaseEntered(nextState);
        }

        if (state.getCapabilities().contains(PrincessCapability.class) &&
                "must".equals(state.getStringRule(Rule.PRINCESS_ACTION))) {
            ReturnMeepleAction princessAction = (ReturnMeepleAction) actions.find(a -> a instanceof ReturnMeepleAction && ((ReturnMeepleAction) a).getSource() == ReturnMeepleSource.PRINCESS).getOrNull();
            if (princessAction != null) {
                actions = Vector.of(princessAction);
            }
        }

        return promote(nextState);
    }

    @PhaseMessageHandler
    public StepResult handleMoveNeutralFigure(GameState state, MoveNeutralFigureMessage msg) {
        BoardPointer ptr = msg.getTo();
        NeutralFigure<?> fig = state.getNeutralFigures().getById(msg.getFigureId());
        if (fig instanceof Fairy) {
            // TODO validation against ActionState

            assert ("on-tile".equals(state.getStringRule(Rule.FAIRY_PLACEMENT)) ? Position.class : BoardPointer.class).isInstance(ptr);

            Fairy fairy = (Fairy) fig;
            state = (new MoveNeutralFigure<BoardPointer>(fairy, ptr, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            return next(state);
        }
        throw new IllegalArgumentException("Illegal neutral figure move");
    }

    @PhaseMessageHandler
    public StepResult handleReturnMeeple(GameState state, ReturnMeepleMessage msg) {
        MeeplePointer ptr = msg.getPointer();

        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));

        switch (msg.getSource()) {
        case PRINCESS:
            ReturnMeepleAction princessAction = (ReturnMeepleAction) state.getPlayerActions()
                .getActions().find(a -> a instanceof ReturnMeepleAction && ((ReturnMeepleAction) a).getSource() == ReturnMeepleSource.PRINCESS)
                .getOrElseThrow(() -> new IllegalArgumentException("Return meeple is not allowed"));
            if (princessAction.getOptions().contains(ptr)) {
                state = state.addFlag(Flag.PRINCESS_USED);
            } else {
                throw new IllegalArgumentException("Pointer doesn't match princess action");
            }
            break;
        case FESTIVAL:
            if (!state.getLastPlaced().getTile().hasModifier(FestivalCapability.FESTIVAL)) {
                throw new IllegalArgumentException("Festival return is not allowed");
            }
            break;
        default:
            throw new IllegalArgumentException("Return meeple is not allowed");
        }

        state = (new UndeployMeeple(meeple, true)).apply(state);
        state = clearActions(state);
        return next(state);
    }

    private StepResult handlePlaceTower(GameState state, PlaceTokenMessage msg) {
        FeaturePointer ptr = (FeaturePointer) msg.getPointer();
        Tower tower = (Tower) state.getFeatureMap().get(ptr).getOrElseThrow(() -> new IllegalArgumentException("No tower"));
        tower = tower.increaseHeight();

        state = state.setFeatureMap(state.getFeatureMap().put(ptr, tower));
        state = state.appendEvent(new TokenPlacedEvent(
            PlayEventMeta.createWithActivePlayer(state), TowerToken.TOWER_PIECE, ptr)
        );

        state = clearActions(state);
        return next(state, TowerCapturePhase.class);
    }

    private StepResult handlePlaceBridge(GameState state, PlaceTokenMessage msg) {
        FeaturePointer ptr = (FeaturePointer) msg.getPointer();
        state = (new PlaceBridge(ptr)).apply(state);
        state = clearActions(state);
        return enter(state);
    }

    private StepResult handlePlaceTunnel(GameState state, PlaceTokenMessage msg) {
        Tunnel token = (Tunnel) msg.getToken();
        FeaturePointer ptr = (FeaturePointer) msg.getPointer();
        state = (new PlaceTunnel(token, ptr)).apply(state);
        state = clearActions(state);
        return enter(state);
    }

    private StepResult handlePlaceLittleBuilding(GameState state, PlaceTokenMessage msg) {
    	LittleBuilding token = (LittleBuilding) msg.getToken();
        Position pos = (Position) msg.getPointer();
        state = (new PlaceLittleBuilding(token, pos)).apply(state);
        state = clearActions(state);
        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Player player = state.getActivePlayer();
        Token token = msg.getToken();

        state = state.mapPlayers(ps ->
            ps.addTokenCount(player.getIndex(), token, -1)
        );

        if (token instanceof Tunnel) {
        	return handlePlaceTunnel(state, msg);
        }
        if (token instanceof LittleBuilding) {
        	return handlePlaceLittleBuilding(state, msg);
        }
        if (token instanceof TowerToken) {
        	return handlePlaceTower(state, msg);
        }
        if (token instanceof BrigeToken) {
        	return handlePlaceBridge(state, msg);
        }
        throw new IllegalArgumentException(String.format("%s placement is not allowed", token));
    }
}
