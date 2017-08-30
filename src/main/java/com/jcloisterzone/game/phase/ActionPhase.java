package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.reducers.PlaceBridge;
import com.jcloisterzone.reducers.PlaceTunnel;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


public class ActionPhase extends AbstractActionPhase {

    public ActionPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();

        Vector<Class<? extends Meeple>> meepleTypes = Vector.of(
            SmallFollower.class, BigFollower.class, Phantom.class,
            Wagon.class, Mayor.class, Builder.class, Pig.class
        );
;
        Vector<PlayerAction<?>> actions = prepareMeepleActions(state, meepleTypes);

        GameState nextState = state.setPlayerActions(
            new ActionsState(player, actions, true)
        );

        for (Capability<?> cap : nextState.getCapabilities().toSeq()) {
            nextState = cap.onActionPhaseEntered(nextState);
        }

        if (state.getCapabilities().contains(PrincessCapability.class) &&
            state.getBooleanValue(CustomRule.PRINCESS_MUST_REMOVE_KNIGHT)) {
            PrincessAction princessAction = (PrincessAction) actions.find(a -> a instanceof PrincessAction).getOrNull();
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

            assert (state.getBooleanValue(CustomRule.FAIRY_ON_TILE) ? Position.class : BoardPointer.class).isInstance(ptr);

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
            PrincessAction princessAction = (PrincessAction) state.getPlayerActions()
                .getActions().find(Predicates.instanceOf(PrincessAction.class))
                .getOrElseThrow(() -> new IllegalArgumentException("Return meeple is not allowed"));
            if (princessAction.getOptions().contains(ptr)) {
                state = state.addFlag(Flag.PRINCESS_USED);
            } else {
                throw new IllegalArgumentException("Pointer doesn't match princess action");
            }
            break;
        case FESTIVAL:
            if (state.getLastPlaced().getTile().getTrigger() != TileTrigger.FESTIVAL) {
                throw new IllegalArgumentException("Festival return is not allowed");
            }
            break;
        default:
            throw new IllegalArgumentException("Return meeple is not allowed");
        }

        state = (new UndeployMeeple(meeple)).apply(state);
        state = clearActions(state);
        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Player player = state.getActivePlayer();

        FeaturePointer ptr = msg.getPointer();
        Token token = msg.getToken();

        state = state.mapPlayers(ps ->
            ps.addTokenCount(player.getIndex(), token, -1)
        );

        switch (token) {
        case TOWER_PIECE:
            // TODO validation against ActionState
            Tower tower = (Tower) state.getFeatureMap().get(ptr).getOrElseThrow(() -> new IllegalArgumentException("No tower"));
            tower = tower.increaseHeight();

            state = state.setFeatureMap(state.getFeatureMap().put(ptr, tower));
            state = state.appendEvent(new TokenPlacedEvent(
                PlayEventMeta.createWithActivePlayer(state), token, ptr)
            );

            state = clearActions(state);
            return next(state, TowerCapturePhase.class);
        case BRIDGE:
            state = (new PlaceBridge(msg.getPointer())).apply(state);
            state = clearActions(state);
            return enter(state);
        case TUNNEL_A:
        case TUNNEL_B:
        case TUNNEL_C:
            state = (new PlaceTunnel(token, msg.getPointer())).apply(state);
            state = clearActions(state);
            return enter(state);
        default:
            throw new IllegalArgumentException(String.format("%s placement is not allowed", token));
        }
    }


//    @Override
//    public void placeLittleBuilding(LittleBuilding lbType) {
//        GameState state = game.getState();
//        //TODO
//        LittleBuildingsCapability lbCap = game.get(LittleBuildingsCapability.class);
//        lbCap.placeLittleBuilding(getActivePlayer(), lbType);
//
//        state = clearActions(state);
//        next(state);
//    }
//
//
//    @PhaseMessageHandler
//	  // TODO move into AbstactActionPhase!!!
//    public void handleDeployFlier(DeployFlierMessage msg) {
//        game.updateRandomSeed(msg.getCurrentTime());
//        int distance = game.getRandom().nextInt(3) + 1;
//        flierCap.setFlierUsed(true);
//        flierCap.setFlierDistance(msg.getMeepleTypeClass(), distance);
//        game.post(new FlierRollEvent(getActivePlayer(), getTile().getPosition(), distance));
//        next(FlierActionPhase.class);
//    }
}
