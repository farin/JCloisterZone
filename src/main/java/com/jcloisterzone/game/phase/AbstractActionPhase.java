package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.FlierRollEvent;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import com.jcloisterzone.io.message.PayRansomMessage;
import io.vavr.Tuple2;
import io.vavr.collection.*;

public abstract class AbstractActionPhase extends Phase {

    public AbstractActionPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    private boolean isMeepleDeploymentAllowedByCapabilities(GameState state, Position pos) {
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            if (!cap.isMeepleDeploymentAllowed(state, pos)) {
                return false;
            }
        }
        return true;
    }

    private Stream<Tuple2<FeaturePointer, Structure>> getAvailableStructures(GameState state, Stream<PlacedTile> tiles, Set<Position> allowCompletedOn) {
        return tiles.flatMap(tile -> {
            Position pos = tile.getPosition();
            //boolean isCurrentTile = pos.equals(currentTilePos);

            if (!isMeepleDeploymentAllowedByCapabilities(state, pos)) {
                return Stream.empty();
            }

            Stream<Tuple2<Location, Structure>> places = state.getTileFeatures2(pos, Structure.class);

            places = places.filter(t -> !(t._2 instanceof Castle) && !(t._2 instanceof SoloveiRazboynik));

            if (!state.getBooleanRule(Rule.FARMERS)) {
                places = places.filter(t -> !(t._2 instanceof Farm));
            }

            // towers are handled by Tower capability separately (needs collect towers on all tiles)
            // (and flier or magic portal use is also not allowed to be placed on tower
            places = places.filter(t -> !(t._2 instanceof Tower));

            // Placing as abbot is implemented through virtual MONASTERY location.
            places = places.flatMap(t -> {
                Structure struct = t._2;
                if (struct instanceof Cloister && ((Cloister)struct).isMonastery()) {
                    return List.of(t, new Tuple2<>(Location.MONASTERY, t._2));
                }
                return List.of(t);
            });

            boolean allowCompleted = allowCompletedOn.contains(pos);

            if (!allowCompleted) {
                //exclude completed
                places = places.filter(t -> {
                    if (t._1 == Location.MONASTERY) {
                        // monastery is never completed
                        return true;
                    }
                    return !(t._2 instanceof Completable) || ((Completable)t._2).isOpen(state);
                });
            }

            if (state.hasFlag(Flag.FLYING_MACHINE_USED) || !allowCompleted) {
                places = places.filter(t -> t._1 != Location.FLYING_MACHINE);
            }

            return places.map(t -> t.map1(loc -> new FeaturePointer(pos, loc)));
        });
    }

    private Set<FeaturePointer> getMeepleAvailableStructures(GameState state, Meeple meeple, Stream<Tuple2<FeaturePointer, Structure>> structures, boolean includeOccupied) {
        if (!includeOccupied) {
            structures = structures.filter(t -> {
                if (meeple instanceof Special) {
                    return true;
                }
                Structure struct = t._2;
                Stream<Meeple> meeples;
                if (struct instanceof Cloister) {
                    meeples = ((Cloister) struct).getMeeplesIncludingMonastery(state);
                } else {
                    meeples = struct.getMeeples(state);
                }

                // Shepherd is not interacting with other meeples
                if (meeples.find(m -> !(m instanceof Shepherd)).isEmpty()) {
                    // no meeples except Shepherd is on feature
                    return true;
                }
                if (struct instanceof Road && ((Road) struct).isLabyrinth()) {
                    // find if there is empty labyrinth segment
                    Set<FeaturePointer> segment = ((Road) struct).findSegmentBorderedBy(state, t._1,
                            fp -> ((Road)state.getPlacedTile(fp.getPosition()).getInitialFeaturePartOf(fp.getLocation())).isLabyrinth()).toSet();
                    boolean segmentIsEmpty = Stream.ofAll(state.getDeployedMeeples())
                            .filter(x -> !(x._1 instanceof Shepherd))
                            .filter(x -> segment.contains(x._2))
                            .isEmpty();
                    if (segmentIsEmpty) {
                        // whole road is occupied but segment divided by labyrinth is free
                        return true;
                    }
                }
                return false;
            });
        }

        return structures
            .filter(t -> meeple.isDeploymentAllowed(state, t._1, t._2) == DeploymentCheckResult.OK)
            .map(t -> t._1)
            .toSet();
    }

    protected Vector<PlayerAction<?>> prepareMeepleActions(GameState state, Vector<Class<? extends Meeple>> meepleTypes) {
        Player player = state.getTurnPlayer();
        Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);

        PlacedTile lastPlaced = state.getLastPlaced();
        Position currentTilePos = lastPlaced.getPosition();
        Stream<PlacedTile> tiles;
        Stream<Tuple2<FeaturePointer, Structure>> specialMeepleStructures;
        Stream<Tuple2<FeaturePointer, Structure>> regularMeepleStructures;

        if (lastPlaced.getTile().hasModifier(PortalCapability.MAGIC_PORTAL) && !state.getFlags().contains(Flag.PORTAL_USED)) {
            Stream<PlacedTile> allTiles = Stream.ofAll(state.getPlacedTiles().values());
            regularMeepleStructures = getAvailableStructures(state, allTiles, HashSet.of(currentTilePos));
            specialMeepleStructures = getAvailableStructures(state, Stream.of(lastPlaced), HashSet.of(currentTilePos));
        } else {
            regularMeepleStructures = getAvailableStructures(state, Stream.of(lastPlaced), HashSet.of(currentTilePos));
            specialMeepleStructures = regularMeepleStructures;
        }

        Vector<PlayerAction<?>> actions = availMeeples.map(meeple -> {
            Set<FeaturePointer> locations = getMeepleAvailableStructures(
                    state, meeple,
                    meeple instanceof Special ? specialMeepleStructures : regularMeepleStructures,
                    false);
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

        if (fp.getLocation() == Location.FLYING_MACHINE) {
            return handleDeployFlier(state, msg);
        }

        Meeple meeple = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        PlacedTile placedTile = state.getLastPlaced();

        //TODO validate placement against players actions

        if (fp.getLocation() == Location.FLYING_MACHINE) {
            throw new IllegalArgumentException("Use DEPLOY_FLIER message instead");
        }

        if (fp.getLocation() != Location.TOWER
            && placedTile.getTile().hasModifier(PortalCapability.MAGIC_PORTAL)
            && !fp.getPosition().equals(placedTile.getPosition())
        ) {
            state = state.addFlag(Flag.PORTAL_USED);
        }

        state = (new DeployMeeple(meeple, fp)).apply(state);
        if (meeple instanceof Barn) {
            state = state.setCapabilityModel(BarnCapability.class, fp);
        }

        state = clearActions(state);
        return next(state);
    }

    private Position getTargetPosition(Position pos, Location direction, int distance) {
        for (int i = 0; i < distance; i++) {
            pos = pos.add(direction);
        }
        return pos;
    }

    public StepResult handleDeployFlier(GameState state, DeployMeepleMessage msg) {
        PlacedTile placedTile = state.getLastPlaced();
        FlyingMachine flyingMachine = (FlyingMachine) state.getFeature(msg.getPointer());
        Meeple meeple = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());

        int distance = getRandom().nextInt(3) + 1;
        state = state.addFlag(Flag.FLYING_MACHINE_USED);  // flying machine can't be used again by phantom
        state = state.appendEvent(new FlierRollEvent(
            PlayEventMeta.createWithActivePlayer(state), placedTile.getPosition(), distance)
        );

        Position target = getTargetPosition(placedTile.getPosition(), flyingMachine.getDirection(), distance);
        PlacedTile targetTile = state.getPlacedTile(target);
        if (targetTile == null || !isMeepleDeploymentAllowedByCapabilities(state, target)) {
            // empty place on landing tile
            return next(state);
        }

        Stream<Tuple2<FeaturePointer, Structure>> structures = getAvailableStructures(state, Stream.of(targetTile), HashSet.empty());
        structures = structures.filter(t -> !(t._2 instanceof Farm));
        Set<FeaturePointer> options = getMeepleAvailableStructures(state, meeple, structures, true);

        if (options.isEmpty()) {
            return next(state);
        }

        PlayerAction<?> action = new MeepleAction(meeple, options);
        state = state.setPlayerActions(new ActionsState(state.getTurnPlayer(), action, false));
        return promote(state);
    }
}
