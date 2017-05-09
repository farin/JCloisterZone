package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.play.BridgePlaced;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TileDiscardedEvent;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceBridge;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;


public class TilePhase extends Phase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;

    public TilePhase(GameController gc) {
        super(gc);
        DebugConfig debugConfig = getDebugConfig();
        if (debugConfig != null) {
            List<String> draw = debugConfig.getDraw();
            if (draw != null && !draw.isEmpty()) {
                debugTiles = new ArrayList<String>(draw);
            }
        }
    }

    public GameState drawTile(GameState state, int index) {
        TilePack tps = state.getTilePack();
        Tuple2<TileDefinition, TilePack> t = tps.drawTile(index);
        return state.setTilePack(t._2).setDrawnTile(t._1);
    }

    public GameState drawTile(GameState state, String tileId) {
        TilePack tps = state.getTilePack();
        Tuple2<TileDefinition, TilePack> t = tps.drawTile(tileId);
        return state.setTilePack(t._2).setDrawnTile(t._1);
    }

    private String pullDebugDrawTileId() {
//        boolean riverActive = tilePack.getGroupState("river-start") == TileGroupState.ACTIVE || tilePack.getGroupState("river") == TileGroupState.ACTIVE;
//        if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && riverActive) {
//            game.getCapability(RiverCapability.class).activateNonRiverTiles();
//            tilePack.setGroupState("river-start", TileGroupState.RETIRED);
//            game.setCurrentTile(tile); //recovery from lake placement
//        }
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
            return debugTiles.remove(0);
        }
        return null;
    }


    private boolean isDebugForcedEnd() {
        return debugTiles != null && !debugTiles.isEmpty() && debugTiles.get(0).equals(DEBUG_END_OF_PACK);
    }

    @Override
    public StepResult enter(GameState state) {
        for (;;) {
            BazaarCapabilityModel bazaarModel = state.getCapabilityModel(BazaarCapability.class);
            Queue<BazaarItem> bazaarSupply = bazaarModel == null ? null : bazaarModel.getSupply();

            if (bazaarSupply != null && !bazaarSupply.isEmpty()) { // can be empty when reinvoked after discard/pass
                Tuple2<BazaarItem, Queue<BazaarItem>> t = bazaarSupply.dequeue();
                BazaarItem item = t._1;
                if (item.getOwner().equals(state.getTurnPlayer())) {
                    bazaarSupply = t._2;
                    bazaarModel = bazaarModel.setSupply(bazaarSupply);
                    state = state.setCapabilityModel(BazaarCapability.class, bazaarModel);
                    state = state.setDrawnTile(item.getTile());
                } /* else:
                    very rare case when not legal placement has been found for prev bazaar tile
                    (Or player pass tile placement (allowed when only legal placement is with bridge)
                    Draw random tile instead
                */
            }

            if (state.getDrawnTile() == null) {
                // regular flow (not tile from Bazaar supply
                TilePack tilePack = state.getTilePack();
                boolean packIsEmpty = tilePack.isEmpty() || isDebugForcedEnd();

                if (packIsEmpty && bazaarSupply != null) {
                    // Very edge case: no match for bazaar tile + no remaining tile in pack.
                    // Skip player's turns
                    return next(state, CleanUpTurnPhase.class);
                }

                //Abbey special case, every player has opportunity to place own abbey at the end.
                if (packIsEmpty && state.getCapabilities().contains(AbbeyCapability.class)) {
                    Integer endPlayerIdx = state.getCapabilityModel(AbbeyCapability.class);
                    Player turnPlayer = state.getTurnPlayer();
                    if (endPlayerIdx == null) {
                        //tile pack has been depleted jut now
                        endPlayerIdx = turnPlayer.getPrevPlayer(state).getIndex();
                        state = state.setCapabilityModel(AbbeyCapability.class, endPlayerIdx);
                    }
                    if (endPlayerIdx != turnPlayer.getIndex()) {
                        return next(state, CleanUpTurnPartPhase.class);
                    }
                    // otherwise proceed to game over
                }

                // Tile Pack is empty
                if (packIsEmpty) {
                    return next(state, GameOverPhase.class);
                }

                // Handle forced debug draw
                String debugDrawTileId = pullDebugDrawTileId();
                boolean makeRegularDraw = true;
                if (debugDrawTileId != null) {
                    try {
                        state = drawTile(state, debugDrawTileId);
                        makeRegularDraw = false;
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid debug draw id: " + debugDrawTileId);
                    }
                }

                if (makeRegularDraw) {
                    int rndIndex = game.getRandom().nextInt(tilePack.size());
                    state = drawTile(state, rndIndex);
                }
            }

            TileDefinition tile = state.getDrawnTile();
            Set<TilePlacement> placements = state.getTilePlacements(tile).toSet();

            if (placements.isEmpty()) {
                state = discardTile(state);

                //if (riverCap != null) riverCap.turnPartCleanUp(); //force group activation if neeeded
            } else {
                TilePlacementAction action = new TilePlacementAction(tile, placements);

                boolean canPass = placements.find(p -> p.getMandatoryBridge() == null).isEmpty();

                state = state.setPlayerActions(new ActionsState(
                    state.getTurnPlayer(),
                    Vector.of(action),
                    canPass
                ));

                return promote(state);
            }
        }
    }

    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        TilePlacementAction action = (TilePlacementAction) state.getAction();

        if (action.getOptions().find(p -> p.getMandatoryBridge() == null).isDefined()) {
            throw new IllegalStateException("Pass is not allowed");
        }

        state = discardTile(state);
        state = state.setDrawnTile(null);
        return enter(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTile(GameState state, PlaceTileMessage msg) {
        game.markUndo();
        TileDefinition tile = state.getDrawnTile();
        Position pos = msg.getPosition();
        Rotation rot = msg.getRotation();
        Player player = state.getActivePlayer();

        assert tile.getId().equals(msg.getTileId());

        TilePlacementAction action = (TilePlacementAction) state.getPlayerActions().getActions().get();

        TilePlacement placement = action.getOptions()
            .find(tp -> tp.getPosition().equals(pos) && tp.getRotation().equals(rot))
            .getOrElseThrow(() -> new IllegalArgumentException("Invalid placement " + pos + "," + rot));

        FeaturePointer mandatoryBridge = placement.getMandatoryBridge();

        if (mandatoryBridge != null) {
            state = state.mapPlayers(ps ->
                ps.addTokenCount(player.getIndex(), Token.BRIDGE, -1)
            );
            state = state.mapCapabilityModel(BridgeCapability.class, model -> model.add(mandatoryBridge));

            Position bridgePos = mandatoryBridge.getPosition();
            Location bridgeLoc = mandatoryBridge.getLocation();
            if (bridgePos.equals(pos)) {
                //bridge on just placed tile -> just update tile definition
                tile = tile.addBridge(bridgeLoc.rotateCCW(rot));
                state = state.mapCapabilityModel(BridgeCapability.class, model -> model.add(mandatoryBridge));
            } else {
                state = (new PlaceBridge(mandatoryBridge, true)).apply(state);
            }
        }

        state = (new PlaceTile(tile, msg.getPosition(), msg.getRotation())).apply(state);

        if (mandatoryBridge != null) {
            state = state.appendEvent(
                new BridgePlaced(PlayEventMeta.createWithPlayer(player), mandatoryBridge)
            );
        }

        state = clearActions(state);
        state = state.setDrawnTile(null);

        if (tile.getTrigger() == TileTrigger.BAZAAR) {
            BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
            //Do not trigger another auction is current is not resolved
            if (model.getSupply() == null) {
                state = state.addFlag(Flag.BAZAAR_AUCTION);
            }
        }

        return next(state);
    }

    private GameState discardTile(GameState state) {
        TileDefinition tile = state.getDrawnTile();
        return state
            .setDiscardedTiles(state.getDiscardedTiles().append(tile))
            .appendEvent(new TileDiscardedEvent(tile));
    }
}
