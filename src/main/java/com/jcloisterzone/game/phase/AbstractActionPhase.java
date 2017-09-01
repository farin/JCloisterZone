package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public abstract class AbstractActionPhase extends Phase {

    public AbstractActionPhase(Config config, Random random) {
        super(config, random);
    }

    protected Vector<PlayerAction<?>> prepareMeepleActions(GameState state, Vector<Class<? extends Meeple>> meepleTypes) {
        Player player = state.getTurnPlayer();
        Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);

        PlacedTile lastPlaced = state.getLastPlaced();
        Position currentTilePos = lastPlaced.getPosition();
        Stream<PlacedTile> tiles;

        if (lastPlaced.getTile().getTrigger() == TileTrigger.PORTAL && !state.getFlags().contains(Flag.PORTAL_USED)) {
            tiles = Stream.ofAll(state.getPlacedTiles().values());
        } else {
            tiles = Stream.of(lastPlaced);
        }

        Stream<Tuple2<FeaturePointer, Scoreable>> placesFp = tiles.flatMap(tile -> {
            Position pos = tile.getPosition();
            boolean isCurrentTile = pos.equals(currentTilePos);

            boolean placementAllowed = true;
            for (Capability<?> cap : state.getCapabilities().toSeq()) {
                if (!cap.isMeepleDeploymentAllowed(state, pos)) {
                    placementAllowed = false;
                    break;
                }
            }

            Stream<Tuple2<Location, Scoreable>> places;
            if (placementAllowed) {
                places = state.getTileFeatures2(pos, Scoreable.class);
                //places = tile.getScoreables(!isCurrentTile);
            } else {
                places = Stream.empty();
            }

            if (!isCurrentTile) {
                //exclude completed
                places = places.filter(t -> {
                    if (t._2 instanceof Completable) {
                        return !((Completable)t._2).isCompleted(state);
                    } else {
                        return true;
                    }
                });
            }

            return places.map(t -> t.map1(loc -> new FeaturePointer(pos, loc)));
        });

        Vector<PlayerAction<?>> actions = availMeeples.map(meeple -> {
            Set<FeaturePointer> locations = placesFp
                .filter(t -> !t._2.isOccupied(state))
                .filter(t -> meeple.isDeploymentAllowed(state, t._1, t._2) == DeploymentCheckResult.OK)
                .flatMap(t -> {
                    if (t._2 instanceof Cloister && ((Cloister)t._2).isMonastery()) {
                        return List.of(
                            t,
                            t.update1(new FeaturePointer(t._1.getPosition(), Location.ABBOT))
                        );
                    } else {
                        return List.of(t);
                    }
                })
                .map(t -> t._1)
                .toSet();

            PlayerAction<?> action = new MeepleAction(meeple, locations);
            return action;
        });

        return actions.filter(action -> !action.isEmpty());
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePayRansom(GameState state, PayRansomMessage msg) {
        state = (new PayRansom(msg.getMeepleId())).apply(state);
        return enter(state); //recompute actions with returned followers
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        //TODO validate against players actions instead
        if (m instanceof Follower) {
            if (state.getFeature(fp).isOccupied(state)) {
                throw new IllegalArgumentException("Feature is occupied.");
            }
        }
        PlacedTile placedTile = state.getLastPlaced();

        state = (new DeployMeeple(m, fp)).apply(state);

        if (fp.getLocation() != Location.TOWER
            && placedTile.getTile().getTrigger() == TileTrigger.PORTAL
            && !fp.getPosition().equals(placedTile.getPosition())
        ) {
            state = state.addFlag(Flag.PORTAL_USED);
        }

        if (m instanceof Barn) {
            state = state.setCapabilityModel(BarnCapability.class, fp);
        }

        state = clearActions(state);
        return next(state);
    }
}
