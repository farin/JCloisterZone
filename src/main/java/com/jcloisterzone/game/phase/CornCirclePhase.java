package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CornCircleSelectDeployOrRemoveAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCicleOption;

import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

@RequiredCapability(CornCircleCapability.class)
public class CornCirclePhase extends Phase {

    public CornCirclePhase(Config config, Random random) {
        super(config, random);
    }

    private Class<? extends Feature> getCornType(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        return  placedTile.getTile().getCornCircle();
    }


    @Override
    public StepResult enter(GameState state) {
        Class<? extends Feature> cornType = getCornType(state);

        // if no follower is deployed corn circle has not effective result for both choices, we can skip it
        if (cornType == null || state.getDeployedMeeples().isEmpty()) {
            return next(state);
        }

        CornCircleSelectDeployOrRemoveAction action = new CornCircleSelectDeployOrRemoveAction();
        ActionsState as = new ActionsState(state.getTurnPlayer(), action, false);
        return promote(state.setPlayerActions(as));
    }

    @PhaseMessageHandler
    public StepResult handleCornCircleRemoveOrDeployMessage(GameState state, CornCircleRemoveOrDeployMessage msg) {
        state = state.setCapabilityModel(CornCircleCapability.class, msg.getValue());

        Player player = state.getTurnPlayer().getNextPlayer(state);
        return createAction(state, player);
    }

    private boolean isLast(GameState state, Player player) {
        return state.getTurnPlayer().equals(player);
    }

    private StepResult endPhase(GameState state) {
        state = clearActions(state);
        return next(state);
    }

    private StepResult nextCornPlayer(GameState state, Player player) {
        if (isLast(state, player)) {
            return endPhase(state);
        } else {
            return createAction(state, player.getNextPlayer(state));
        }
    }

    private StepResult createAction(GameState state, Player player) {
        CornCicleOption option = state.getCapabilityModel(CornCircleCapability.class);
        Class<? extends Feature> cornType = getCornType(state);

        //Meeple, FeaturePointe
        Stream<Tuple2<Meeple, FeaturePointer>> meeples = Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> t._1.getPlayer().equals(player))
            .filter(t -> cornType.isInstance(state.getFeature(t._2)));
            //.map(Tuple2::_2)
           // .toSet();

        if (meeples.isEmpty()) {
            return nextCornPlayer(state, player);
        }

        Vector<PlayerAction<?>> actions = null;

        switch (option) {
        case DEPLOY:
            Vector<Class<? extends Meeple>> meepleTypes = Vector.of(SmallFollower.class, BigFollower.class, Phantom.class);
            if (!cornType.equals(Farm.class)) {
                meepleTypes = meepleTypes.append(Wagon.class);
            }
            if (cornType.equals(City.class)) {
                meepleTypes = meepleTypes.append(Mayor.class);
            }

            Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);
            if (availMeeples.isEmpty()) {
                return nextCornPlayer(state, player);
            }

            Set<FeaturePointer> options = meeples.map(Tuple2::_2).toSet();
            actions = availMeeples.map(meeple ->
                new MeepleAction(meeple.getClass(), options)
            );
            break;
        case REMOVE:
            throw new UnsupportedOperationException("TODO");
            //break;
        }

        return promote(state.setPlayerActions(
            new ActionsState(player, actions, option == CornCicleOption.DEPLOY)
        ));
    }

//    private void nextCornPlayer() {
//        Player active = getActivePlayer();
//        if (active == game.getTurnPlayer()) {
//            cornCircleCap.setCornCirclePlayer(null);
//            cornCircleCap.setCornCircleOption(null);
//            next();
//        } else {
//            Player cornPlayer = game.getNextPlayer(active);
//            cornCircleCap.setCornCirclePlayer(cornPlayer);
//            prepareCornAction();
//        }
//    }
//

//
//    private void prepareCornAction() {
//        List<PlayerAction<?>> actions;
//        Class<? extends Feature> cornType = getTile().getCornCircle();
//        if (cornCircleCap.getCornCircleOption() == CornCicleOption.REMOVAL) {
//            actions = prepareRemovalAction(cornType);
//        } else {
//            actions = prepareDeploymentAction(cornType);
//        }
//        if (actions.isEmpty()) {
//            nextCornPlayer();
//        } else {
//            boolean passAllowed = cornCircleCap.getCornCircleOption() == CornCicleOption.DEPLOYMENT;
//            Player activePlayer = getActivePlayer();
//            toggleClock(activePlayer);
//            game.post(new SelectActionEvent(activePlayer, actions, passAllowed));
//        }
//    }
//
//    private List<PlayerAction<?>> prepareDeploymentAction(Class<? extends Feature> cornType) {
//        List<FeaturePointer> pointers = new ArrayList<>();
//        for (Meeple m : game.getDeployedMeeples()) {
//            if (!(m instanceof Follower)) continue;
//            if (m.getPlayer() != getActivePlayer()) continue;
//            if (!cornType.isInstance(m.getFeature())) continue;
//            pointers.add(new FeaturePointer(m.getPosition(), m.getLocation()));
//        }
//        if (pointers.isEmpty()) return Collections.emptyList();
//
//        List<PlayerAction<?>> actions = new ArrayList<>();
//        //TODO nice to do this in generic way independently on particular followers enumeration
//        if (getActivePlayer().hasFollower(SmallFollower.class)) {
//            actions.add(new MeepleAction(SmallFollower.class).addAll(pointers));
//        }
//        if (getActivePlayer().hasFollower(BigFollower.class)) {
//            actions.add(new MeepleAction(BigFollower.class).addAll(pointers));
//        }
//        if (getActivePlayer().hasFollower(Phantom.class)) {
//            actions.add(new MeepleAction(Phantom.class).addAll(pointers));
//        }
//        if (cornType.equals(City.class) && getActivePlayer().hasFollower(Mayor.class)) {
//            actions.add(new MeepleAction(Mayor.class).addAll(pointers));
//        }
//        if (!cornType.equals(Farm.class) && getActivePlayer().hasFollower(Wagon.class)) {
//            actions.add(new MeepleAction(Wagon.class).addAll(pointers));
//        }
//        return actions;
//    }
//
//    private List<PlayerAction<?>> prepareRemovalAction(Class<? extends Feature> cornType) {
//        UndeployAction action = null;
//        for (Meeple m : game.getDeployedMeeples()) {
//            if (!(m instanceof Follower)) continue;
//            if (m.getPlayer() != getActivePlayer()) continue;
//            if (!cornType.isInstance(m.getFeature())) continue;
//            if (action == null) {
//                action = new UndeployAction("undeploy");
//            }
//            action.add(new MeeplePointer(m));
//        }
//        if (action == null) return Collections.emptyList();
//        return Collections.<PlayerAction<?>>singletonList(action);
//    }
//
//    @WsSubscribe
//    public void handleReturnMeeple(ReturnMeepleMessage msg) {
//        if (cornCircleCap.getCornCircleOption() != CornCicleOption.REMOVAL) {
//            logger.error("Removal not selected as corn options.");
//            return;
//        }
//        Meeple m = game.getMeeple(mp);
//        Class<? extends Feature> cornType = getTile().getCornCircle();
//        if (!cornType.isInstance(m.getFeature())) {
//            logger.error("Improper feature type");
//            return;
//        }
//        m.undeploy();
//        nextCornPlayer();
//    }
//
//    @Override
//    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
//        if (cornCircleCap.getCornCircleOption() != CornCicleOption.DEPLOYMENT) {
//            logger.error("Deployment wasn't selected as corn options.");
//            return;
//        }
//        List<Meeple> meeples = getBoard().getPlayer(fp).getMeeples();
//        if (meeples.isEmpty()) {
//            logger.error("Feature must be occupied");
//            return;
//        }
//        if (meeples.get(0).getPlayer() != getActivePlayer()) {
//            logger.error("Feature must be occupies with own follower");
//            return;
//        }
//
//        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
//        m.deploy(fp);
//        nextCornPlayer();
//    }
//
//    @WsSubscribe
//    public void handlePass(PassMessage msg) {
//        if (cornCircleCap.getCornCircleOption() == CornCicleOption.REMOVAL) {
//            logger.error("Removal cannot be passed");
//            return;
//        }
//        nextCornPlayer();
//    }
}
