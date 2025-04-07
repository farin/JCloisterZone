package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.ScoreAcrobatsAction;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.feature.Acrobats;
import com.jcloisterzone.feature.Monastic;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability.BridgeToken;
import com.jcloisterzone.game.capability.AcrobatsCapability;
import com.jcloisterzone.game.capability.FestivalCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability.TowerToken;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ScoreAcrobatsMessage;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.*;
import io.vavr.collection.Vector;


public class ActionPhase extends AbstractActionPhase {

    private TowerCapturePhase towerCapturePhase;

    public ActionPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
        this.towerCapturePhase = new TowerCapturePhase(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();

        Vector<Class<? extends Meeple>> meepleTypes = Vector.of(
            SmallFollower.class, BigFollower.class, Phantom.class, Abbot.class,
            Wagon.class, Mayor.class, Builder.class, Pig.class, Shepherd.class,
            Ringmaster.class
        );

        Vector<PlayerAction<?>> actions = prepareMeepleActions(state, meepleTypes);

        state = state.setPlayerActions(
            new ActionsState(player, actions, true)
        );

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onActionPhaseEntered(state);
        }

        if (state.getCapabilities().contains(PrincessCapability.class) &&
                "must".equals(state.getStringRule(Rule.PRINCESS_ACTION))) {
            ReturnMeepleAction princessAction = (ReturnMeepleAction) state.getPlayerActions().getActions().
                    find(a -> a instanceof ReturnMeepleAction && ((ReturnMeepleAction) a).getSource() == ReturnMeepleSource.PRINCESS).getOrNull();
            if (princessAction != null) {
                actions = Vector.of(princessAction);
                state = state.setPlayerActions(new ActionsState(player, actions, false));
            }
        }
        
        state = state.setPlayerActions(state.getPlayerActions().reorderActions());

        if (state.getPlayerActions().getActions().isEmpty()) {
            state = clearActions(state);
            return next(state);
        }

        return promote(state);
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

        Monastic assignAbbotScore = null;

        switch (msg.getSource()) {
            case PRINCESS:
            case ROBBERS_SON:
                ReturnMeepleAction princessAction = (ReturnMeepleAction) state.getPlayerActions()
                    .getActions().find(a -> a instanceof ReturnMeepleAction && ((ReturnMeepleAction) a).getSource() == msg.getSource())
                    .getOrElseThrow(() -> new IllegalArgumentException("Return meeple is not allowed"));
                if (princessAction.getOptions().contains(ptr)) {
                    state = state.addFlag(Flag.NO_PHANTOM);
                } else {
                    throw new IllegalArgumentException("Pointer doesn't match return action");
                }
                break;
            case FESTIVAL:
                if (!state.getLastPlaced().getTile().hasModifier(FestivalCapability.FESTIVAL)) {
                    throw new IllegalArgumentException("Festival return is not allowed");
                }
                break;
            case ABBOT_RETURN:
                if (meeple.getPlayer() != state.getPlayerActions().getPlayer() || !(meeple instanceof Abbot)) {
                    throw new IllegalArgumentException("Not abbot owner");
                }
                assignAbbotScore = (Monastic) state.getFeature(ptr.asFeaturePointer());
                break;
            case TRAP:
                if (meeple.getPlayer() != state.getPlayerActions().getPlayer()) {
                    throw new IllegalArgumentException("Not owner");
                }
                break;
            default:
                throw new IllegalArgumentException("Return meeple is not allowed");
        }

        state = (new UndeployMeeple(meeple, true)).apply(state);
        state = clearActions(state);

        if (assignAbbotScore != null) {
            PointsExpression points = assignAbbotScore.getStructurePoints(state, false);
            ReceivedPoints rp = new ReceivedPoints(points, meeple.getPlayer(), ptr.asFeaturePointer());
            state = (new AddPoints(rp, false)).apply(state);
        }

        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handleScoreAcrobatsMessage(GameState state, ScoreAcrobatsMessage msg) {
    	FeaturePointer fp = msg.getPointer();

    	state.getPlayerActions()
              .getActions().find(a -> a instanceof ScoreAcrobatsAction && ((ScoreAcrobatsAction) a).getOptions().contains(fp))
              .getOrElseThrow(() -> new IllegalArgumentException("Invalid SCORE_ACROBATS"));

    	AcrobatsCapability acrobatsCap = state.getCapabilities().get(AcrobatsCapability.class);
    	state = acrobatsCap.scoreAcrobats(state, (Acrobats) state.getFeature(fp), true);
        state = clearActions(state);

        return next(state);
    }

    private StepResult handlePlaceTower(GameState state, PlaceTokenMessage msg) {
        FeaturePointer ptr = (FeaturePointer) msg.getPointer();
        Tower tower = (Tower) state.getFeature(ptr);
        if (tower == null) {
            new IllegalArgumentException("No tower");
        }
        tower = tower.increaseHeight();

        state = state.putFeature(ptr, tower);
        state = state.appendEvent(new TokenPlacedEvent(
            PlayEventMeta.createWithActivePlayer(state), TowerToken.TOWER_PIECE, ptr)
        );

        state = clearActions(state);
        return next(state, towerCapturePhase);
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
        if (token instanceof BridgeToken) {
        	return handlePlaceBridge(state, msg);
        }
        throw new IllegalArgumentException(String.format("%s placement is not allowed", token));
    }
}
