package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CaptureFollowerAction;
import com.jcloisterzone.action.SelectPrisonerToExchangeAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TowerCapability.TowerToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.CaptureMeeple;
import com.jcloisterzone.reducers.PrisonersExchage;
import com.jcloisterzone.io.message.CaptureFollowerMessage;
import com.jcloisterzone.io.message.ExchangeFollowerChoiceMessage;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class TowerCapturePhase extends Phase {

    public TowerCapturePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        TokenPlacedEvent event = (TokenPlacedEvent) state.getEvents().last();
        assert event.getToken() == TowerToken.TOWER_PIECE;

        FeaturePointer ptr = (FeaturePointer) event.getPointer();
        Tower tower = (Tower) state.getFeatureMap().get(ptr).get();
        int towerHeight = tower.getHeight();
        Position towerPosition = ptr.getPosition();

        GameState _state = state;
        Set<MeeplePointer> options = Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> {
                Position pos = t._2.getPosition();
                return
                    (t._1 instanceof Follower) &&
                    (pos.x == towerPosition.x || pos.y == towerPosition.y) &&
                    (pos.squareDistance(towerPosition) <= towerHeight);
            })
            .filter(t -> !(_state.getFeature(t._2) instanceof Castle))
            .map(MeeplePointer::new)
            .toSet();

        if (options.isEmpty()) {
            return next(state);
        }

        Player player = state.getTurnPlayer();
        state = state.setPlayerActions(
            new ActionsState(player, new CaptureFollowerAction(options), true)
        );

        return promote(state);
    }

    @PhaseMessageHandler
    public StepResult handleCaptureFollowerMessage(GameState state, CaptureFollowerMessage msg) {
        //TODO validation against ActionState
        MeeplePointer ptr = msg.getPointer();
        Player player = state.getActivePlayer();

        Follower meeple = (Follower) state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));

        state = (new CaptureMeeple(meeple)).apply(state);

        //ski exchange when own follower has been captured
        if (!player.equals(meeple.getPlayer())) {
            Map<Class<? extends Follower>, List<Follower>> exchange = getPrisonersCapturedBy(state, player, meeple.getPlayer())
                .groupBy(f -> f.getClass());

            if (exchange.size() == 1) {
                //only followers of same type has been captured, exchange automatically
                Follower exchangeFor = exchange.get()._2.get();
                state = (new PrisonersExchage(meeple, exchangeFor)).apply(state);
            } else if (exchange.size() > 1) {
                SelectPrisonerToExchangeAction action =  new SelectPrisonerToExchangeAction(
                    meeple,
                    exchange.values().map(l -> l.get()).toSet()
                );
                state = state.setPlayerActions(new ActionsState(player, action, false));
                return promote(state);
            }
        }

        state = clearActions(state);
        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handleExchangeFollowerChoiceMessage(GameState state, ExchangeFollowerChoiceMessage msg) {
        SelectPrisonerToExchangeAction action = (SelectPrisonerToExchangeAction) state.getPlayerActions().getActions().get();
        Follower follower = action.getJustCapturedFollower();
        Follower exchangeFor = state.getPlayers().findFollower(msg.getMeepleId()).get();

        //TODO validation against ActionState

        state = (new PrisonersExchage(follower, exchangeFor)).apply(state);
        state = clearActions(state);
        return next(state);
    }

    private List<Follower> getPrisonersCapturedBy(GameState state, Player owner, Player jailer) {
        return state.getCapabilityModel(TowerCapability.class)
            .get(jailer.getIndex()).filter(f -> f.getPlayer().equals(owner));
    }
}
