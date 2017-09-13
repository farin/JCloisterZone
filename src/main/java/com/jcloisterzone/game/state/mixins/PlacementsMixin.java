package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.EdgeType;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public interface PlacementsMixin extends BoardMixin, PlayersMixin, CapabilitiesMixin {

    default Stream<Tuple2<Position, EdgePattern>> getAvailablePlacements() {
        java.util.Set<Position> used = new java.util.HashSet<>();
        Map<Position, PlacedTile> placedTiles = getPlacedTiles();

        if (placedTiles.isEmpty()) {
            return Stream.of(
                new Tuple2<>(Position.ZERO, EdgePattern.fromString("????"))
            );
        }

        return Stream.ofAll(placedTiles).flatMap(item -> {
            Position pos = item._1;
            java.util.List<Tuple2<Position, EdgePattern>> avail = new java.util.ArrayList<>(4);
            for (Position offset: Position.ADJACENT.values()) {
                Position adj = pos.add(offset);
                if (!used.contains(adj) && !placedTiles.containsKey(adj)) {
                    avail.add(new Tuple2<Position, EdgePattern>(adj, getEdgePattern(adj)));
                    used.add(adj);
                }
            }
            return avail;
        });
    }

    default Stream<Tuple2<Position, EdgePattern>> getHoles() {
        return getAvailablePlacements().filter(t -> t._2.wildcardSize() == 0);
    }

    default EdgePattern getEdgePattern(Position pos) {
        PlacedTile placed = getPlacedTile(pos);
        if (placed != null) {
            return placed.getEdgePattern();
        }

        return new EdgePattern(
            Position.ADJACENT.map((loc, offset) -> {
                Position adj = pos.add(offset);
                PlacedTile  adjTile = getPlacedTile(adj);
                if (adjTile == null) {
                    return new Tuple2<>(loc, EdgeType.UNKNOWN);
                } else {
                    EdgeType edge = adjTile.getEdgePattern().at(loc.rev());
                    return new Tuple2<>(loc, edge);
                }
            })
         );
    }

    default Stream<PlacementOption> getTilePlacements(Tile tile) {
        boolean playerHasBridge = getPlayers().getPlayerTokenCount(
            getTurnPlayer().getIndex(), Token.BRIDGE) > 0;

        EdgePattern basePattern = tile.getEdgePattern();
        Vector<Tuple2<EdgePattern, Location>> baseBridgePatterns = playerHasBridge ? _This.getBridgePatterns(basePattern) : null;

        return getAvailablePlacements().flatMap(avail -> {
            return Stream.of(Rotation.values())
                .map(rot -> {
                    Position pos = avail._1;
                    EdgePattern border = avail._2;
                    EdgePattern tilePattern = basePattern.rotate(rot);
                    if (border.isMatchingExact(tilePattern)) {
                        return new PlacementOption(pos, rot, null);
                    }
                    if (playerHasBridge) {
                        // check bridges on tile
                        for (Tuple2<EdgePattern, Location> t : baseBridgePatterns) {
                            EdgePattern tileWithBridgePattern = t._1.rotate(rot);
                            if (border.isMatchingExact(tileWithBridgePattern)) {
                                Location bridgeLocation = t._2.rotateCW(rot);
                                return new PlacementOption(pos, rot, new FeaturePointer(pos, bridgeLocation));
                            }
                        }
                        // check bridges on adjacent tiles
                        for (Location side : Location.SIDES) {
                            Position adjPos = pos.add(side);
                            if (!getPlacedTiles().containsKey(adjPos)) {
                                continue;
                            }

                            FeaturePointer bridgePtr;
                            if (side == Location.N || side == Location.S) {
                                bridgePtr = new FeaturePointer(adjPos, Location.NS);
                            } else {
                                bridgePtr = new FeaturePointer(adjPos, Location.WE);
                            }

                            // bridge must be legal on adjacent tile
                            if (!isBridgePlacementAllowed(bridgePtr)) {
                                continue;
                            }

                            // and current til edge must be ROAD
                            EdgePattern borderWithBridgePattern = border.replace(side, EdgeType.ROAD);
                            if (borderWithBridgePattern.isMatchingExact(tilePattern)) {
                                return new PlacementOption(pos, rot, bridgePtr);
                            }
                        }
                    }
                    return null;
                })
                .filter(Predicates.isNotNull())
                .filter(tp -> {
                    for (Capability<?> cap : getCapabilities().toSeq()) {
                        if (!cap.isTilePlacementAllowed((GameState) this, tile, tp)) return false;
                    }
                    return true;
                });
        });
    }

    default boolean isBridgePlacementAllowed(FeaturePointer bridgePtr) {
        Position pos = bridgePtr.getPosition();
        Location loc = bridgePtr.getLocation();

        // for valid placement there must be adjacent place with empty
        // space on the other side
        boolean adjExists = loc.splitToSides()
                .map(l -> getPlacedTile(pos.add(l)))
                .find(Predicates.isNotNull())
                .isDefined();

        if (adjExists) {
            return false;
        }

        // also no bridge must be already placed on adjacent tile
        Set<FeaturePointer> placedBridges = getCapabilityModel(BridgeCapability.class);
        if (placedBridges.find(fp -> fp.getPosition().equals(pos)).isDefined()) {
            return false;
        }

        //and bridge must be legal on tile
        PlacedTile placedTile = getPlacedTile(pos);
        return placedTile.getEdgePattern().isBridgeAllowed(loc);
    }


    // Helper methods

    class _This {
        private static Vector<Tuple2<EdgePattern, Location>> getBridgePatterns(EdgePattern basePattern) {
            Vector<Tuple2<EdgePattern, Location>> patterns = Vector.empty();
            for (Location loc : Location.BRIDGES) {
                if (basePattern.isBridgeAllowed(loc)) {
                    patterns = patterns.append(new Tuple2<>(basePattern.getBridgePattern(loc), loc));
                }
            }
            return patterns;
        }
    }

}
