package com.jcloisterzone.engine;

import com.google.gson.*;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.*;
import com.jcloisterzone.board.*;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.*;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.game.capability.GoldminesCapability.GoldToken;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.capability.SheepToken;
import com.jcloisterzone.game.phase.DragonMovePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.RussianPromosTrapPhase;
import com.jcloisterzone.game.state.*;
import com.jcloisterzone.io.MessageParser;
import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.lang.reflect.Type;

public class StateGsonBuilder {

    /*
        TODO clean up spaghetti
        ideas:
            - use same action serializer with params for similar actions
            - clean up play events serialization, use map for events mapping
     */

    public Gson create() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Game.class, new GameSerializer());
        builder.registerTypeHierarchyAdapter(TilePack.class, new TilePackSerializer());
        builder.registerTypeAdapter(ActionsState.class, new ActionsStateSerializer());
        builder.registerTypeAdapter(Position.class, new MessageParser.PositionSerializer());
        builder.registerTypeAdapter(Location.class, new MessageParser.LocationSerializer());
        builder.registerTypeAdapter(FeaturePointer.class, new MessageParser.FeaturePointerSerializer());
        builder.registerTypeAdapter(BoardPointer.class, new MessageParser.BoardPointerSerializer());
        // actions
        builder.registerTypeAdapter(TilePlacementAction.class, new TilePlacementActionSerializer());
        builder.registerTypeAdapter(MeepleAction.class, new MeepleActionSerializer());
        builder.registerTypeAdapter(ReturnMeepleAction.class, new ReturnMeepleActionSerializer());
        builder.registerTypeAdapter(NeutralFigureAction.class, new NeutralFigureActionSerializer());
        builder.registerTypeAdapter(MoveDragonAction.class, new MoveDragonActionSerializer());
        builder.registerTypeAdapter(FairyNextToAction.class, new FairyNextToActionSerializer());
        builder.registerTypeAdapter(FairyOnTileAction.class, new FairyOnTileActionSerializer());
        builder.registerTypeAdapter(TowerPieceAction.class, new TowerPieceActionSerializer());
        builder.registerTypeAdapter(CaptureFollowerAction.class, new CaptureFollowerActionSerializer());
        builder.registerTypeAdapter(SelectPrisonerToExchangeAction.class, new SelectPrisonerToExchangeActionSerializer());
        builder.registerTypeAdapter(BridgeAction.class, new SelectFeatureActionSerializer());
        builder.registerTypeAdapter(CastleAction.class, new SelectFeatureActionSerializer());
        builder.registerTypeAdapter(BazaarSelectTileAction.class, new ActionSerializer("BazaarSelectTile"));
        builder.registerTypeAdapter(BazaarBidAction.class, new ActionSerializer("BazaarBid"));
        builder.registerTypeAdapter(BazaarSelectBuyOrSellAction.class, new ActionSerializer("BazaarSelectBuyOrSell"));
        builder.registerTypeAdapter(CornCircleSelectDeployOrRemoveAction.class, new CornCircleSelectDeployOrRemoveActionSerializer());
        builder.registerTypeAdapter(ConfirmAction.class, new ActionSerializer("Confirm"));
        builder.registerTypeAdapter(FlockAction.class, new FlockActionSerializer());
        builder.registerTypeAdapter(TunnelAction.class, new TunnelActionSerializer());
        builder.registerTypeAdapter(FerriesAction.class, new FerriesActionSerializer());
        builder.registerTypeAdapter(GoldPieceAction.class, new GoldPieceActionSerializer());
        builder.registerTypeAdapter(RemoveMageOrWitchAction.class, new ActionSerializer("RemoveMageOrWitch"));
        builder.registerTypeAdapter(LittleBuildingAction.class, new LittleBuildingActionSerializer());
        builder.registerTypeAdapter(ScoreAcrobatsAction.class, new SelectFeatureActionSerializer());
        return builder.create();
    }

    private int rotationToPrimitive(Rotation rot) {
        return rot.ordinal() * 90;
    }

    private class GameSerializer implements JsonSerializer<Game> {
        public JsonElement serialize(Game game, Type typeOfSrc, JsonSerializationContext context) {
            GameState state = game.getState();
            JsonObject obj = new JsonObject();
            obj.add("players", serializePlayers(state, context));
            obj.add("tilePack", context.serialize(state.getTilePack()));
            obj.add("placedTiles", serializePlacedTiles(state.getPlacedTiles(), context));
            obj.add("discardedTiles", serializeDiscardedTiles(state.getDiscardedTiles(), context));
            obj.add("deployedMeeples", serializeDeployedMeeples(state, context));
            obj.add("neutralFigures", serializeNeutralFigures(state, context));
            obj.add("tokens", serializeTokens(state, context));
            obj.add("features", serializeFeatures(state, context));
            obj.addProperty("phase", getPhaseName(state.getPhase()));
            obj.addProperty("turnPlayer", state.getPlayers().getTurnPlayerIndex());
            obj.add("action", context.serialize(state.getPlayerActions()));
            obj.add("history", serializePlayEvents(state, context));

            JsonObject flags = new JsonObject();
            if (state.hasFlag(Flag.RANSOM_PAID)) {
                flags.addProperty("ransomPaid", true);
            }
            obj.add("flags", flags);

            JsonObject undo = new JsonObject();
            undo.addProperty("allowed", game.isUndoAllowed());
            undo.addProperty("depth", game.getUndoDepth());
            obj.add("undo", undo);

            BazaarCapabilityModel bazModel = game.getState().getCapabilityModel(BazaarCapability.class);
            if (bazModel != null && bazModel.getSupply() != null) {
                final JsonArray bazaar = new JsonArray();
                for (int idx = 0; idx < bazModel.getSupply().length(); idx++) {
                    BazaarItem bi = bazModel.getSupply().get(idx);
                    JsonObject jsonItem = new JsonObject();
                    jsonItem.addProperty("tile", bi.getTile().getId());
                    jsonItem.addProperty("price", bi.getCurrentPrice());
                    jsonItem.addProperty("bidder", bi.getCurrentBidder() == null ? null : bi.getCurrentBidder().getIndex());
                    jsonItem.addProperty("owner", bi.getOwner() == null ? null : bi.getOwner().getIndex());
                    if (bazModel.getAuctionedItemIndex() != null && idx == bazModel.getAuctionedItemIndex()) {
                        jsonItem.addProperty("selectedBy", bazModel.getTileSelectingPlayer().getIndex());
                    }
                    bazaar.add(jsonItem);
                }
                obj.add("bazaar", bazaar);
            }

            SheepCapability sheepCap = state.getCapabilities().get(SheepCapability.class);
            if (sheepCap != null) {
                Map<FeaturePointer, List<SheepToken>> sheepModel = sheepCap.getModel(state).getPlacedTokens();
                JsonObject jsonItem = new JsonObject();
                JsonArray jsonFlocks = new JsonArray();
                sheepModel.forEach((fp, tokens) -> {
                    JsonObject flock = new JsonObject();
                    JsonArray jsonTokens = new JsonArray();
                    tokens.forEach(token -> jsonTokens.add(token.name()));
                    flock.add("position", context.serialize(fp.getPosition()));
                    flock.add("location", context.serialize(fp.getLocation()));
                    flock.add("tokens", jsonTokens);
                    jsonFlocks.add(flock);
                });
                jsonItem.addProperty("bagSize", sheepCap.getBagConent(state).length());
                jsonItem.add("flocks", jsonFlocks);
                obj.add("sheep", jsonItem);
            }

            return obj;
        }
    }

    private String getPhaseName(Phase phase) {
        if (phase instanceof RussianPromosTrapPhase) {
            return "CommitPhase";
        }
        return phase.getClass().getSimpleName();
    }

    private JsonElement serializePlacedTiles(LinkedHashMap<Position, PlacedTile> state, JsonSerializationContext context) {
        JsonArray tiles = new JsonArray();
        state.forEach((pos, placedTile) -> {
            JsonObject obj = new JsonObject();
            obj.add("position", context.serialize(pos));
            obj.addProperty("rotation", rotationToPrimitive(placedTile.getRotation()));
            obj.addProperty("id", placedTile.getTile().getId());
            tiles.add(obj);
        });
        return tiles;
    }

    private JsonElement serializeDiscardedTiles(List<Tile> state, JsonSerializationContext context) {
        JsonArray tiles = new JsonArray();
        state.forEach(t -> {
            tiles.add(t.getId());
        });
        return tiles;
    }


    public JsonElement serializePlayers(GameState root, JsonSerializationContext context) {
        PlayersState state = root.getPlayers();
        Array<List<Follower>> prisoners = root.getCapabilityModel(TowerCapability.class);

        int playerCount = state.getPlayers().length();
        JsonArray players = new JsonArray(playerCount);
        for (int i = 0; i < playerCount; i++) {
            JsonObject player = new JsonObject();
            player.addProperty("points", state.getScore().get(i));

            JsonObject tokens = new JsonObject();
            state.getTokens().get(i).forEach((token, count) -> {
                tokens.addProperty(token.name(), count);
            });
            player.add("tokens", tokens);

            JsonObject meeples = new JsonObject();
            state.getFollowers().get(i).filter(f -> f.isInSupply(root)).groupBy(f -> f.getClass()).forEach((cls, arr) -> {
                JsonArray sizeAndId = new JsonArray(2);
                sizeAndId.add(arr.size());
                sizeAndId.add(arr.get().getId());
                meeples.add(cls.getSimpleName(), sizeAndId);
            });
            state.getSpecialMeeples().get(i).filter(f -> f.isInSupply(root)).groupBy(f -> f.getClass()).forEach((cls, arr) -> {
                JsonArray sizeAndId = new JsonArray(2);
                sizeAndId.add(arr.size());
                sizeAndId.add(arr.get().getId());
                meeples.add(cls.getSimpleName(), sizeAndId);
            });
            player.add("meeples", meeples);

            if (prisoners != null) {
                JsonArray captured = new JsonArray();
                prisoners.get(i).groupBy(f -> new Tuple2<Class<?>, Integer>(f.getClass(), f.getPlayer().getIndex())).forEach((t, arr) -> {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", arr.get().getId());
                    obj.addProperty("type", t._1.getSimpleName());
                    obj.addProperty("player", t._2);
                    obj.addProperty("count", arr.size());
                    captured.add(obj);
                });
                player.add("captured", captured);
            }

            if (state.getTurnPlayerIndex() != null && state.getTurnPlayerIndex() == i) {
                player.addProperty("turn", true);
            }
            players.add(player);
        }
        return players;
    }

    public JsonElement serializeDeployedMeeples(GameState root, JsonSerializationContext context) {
        LinkedHashMap<Meeple, FeaturePointer> state = root.getDeployedMeeples();
        JsonArray meeples = new JsonArray();
        state.forEach((m, fp) -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", m.getId());
            json.addProperty("type", m.getClass().getSimpleName());
            json.addProperty("player", m.getPlayer().getIndex());
            json.add("position", context.serialize(fp.getPosition()));
            json.addProperty("feature", fp.getFeature().getSimpleName());
            json.add("location", context.serialize(fp.getLocation()));
            meeples.add(json);
        });
        return meeples;
    }

    public JsonElement serializeNeutralFigures(GameState root, JsonSerializationContext context) {
        NeutralFiguresState state = root.getNeutralFigures();
        JsonObject neutral = new JsonObject();
        Position pos = state.getDragonDeployment();
        if (pos != null) {
            Vector<Position> visited =  root.getCapabilityModel(DragonCapability.class);
            JsonObject data = new JsonObject();
            data.add("position", context.serialize(pos));
            if (root.getPhase() instanceof DragonMovePhase) {
                JsonArray visitedData = new JsonArray();
                visited.forEach(p -> visitedData.add(context.serialize(p)));
                data.add("visited", visitedData);
                data.addProperty("remaining", DragonCapability.DRAGON_MOVES - visited.length());
            }
            neutral.add("dragon", data);
        }
        BoardPointer bptr = state.getFairyDeployment();
        if (bptr != null) {
            JsonObject data = new JsonObject();
            data.add("placement", context.serialize(bptr));
            neutral.add("fairy", data);
        }
        FeaturePointer fp = state.getMageDeployment();
        if (fp != null) {
            JsonObject data = new JsonObject();
            data.add("placement", context.serialize(fp));
            neutral.add("mage", data);
        }
        fp = state.getWitchDeployment();
        if (fp != null) {
            JsonObject data = new JsonObject();
            data.add("placement", context.serialize(fp));
            neutral.add("witch", data);
        }
        fp = state.getCountDeployment();
        if (fp != null) {
            JsonObject data = new JsonObject();
            data.add("placement", context.serialize(fp));
            neutral.add("count", data);
        }
        pos = state.getBigTopDeployment();
        if (pos != null) {
            JsonObject data = new JsonObject();
            data.add("placement", context.serialize(pos));
            neutral.add("bigtop", data);
        }
        return neutral;
    }

    public JsonElement serializeTokens(GameState root, JsonSerializationContext context) {
        JsonObject tokens = new JsonObject();
        Map<FeaturePointer, PlacedTunnelToken> tunnels = root.getCapabilityModel(TunnelCapability.class);
        if (tunnels != null) {
            Stream.ofAll(tunnels).filter(t -> t._2 != null).map(t -> new Tuple2<>(t._1, t._2.getToken() + "." + t._2.getPlayerIndex())).groupBy(Tuple2::_2).forEach(t -> {
                JsonArray pointers = new JsonArray(t._2.size());
                t._2.map(Tuple2::_1).forEach(fp -> pointers.add(context.serialize(fp)));
                tokens.add(t._1, pointers);
            });
        }
        FerriesCapabilityModel ferriesModel = root.getCapabilityModel(FerriesCapability.class);
        if (ferriesModel != null) {
            JsonArray ferries = new JsonArray();
            ferriesModel.getFerries().forEach(f -> {
                ferries.add(context.serialize(f));
            });
            tokens.add(FerryToken.FERRY.name(), ferries);
        }
        Map<Position, Integer> goldTokens = root.getCapabilityModel(GoldminesCapability.class);
        if (goldTokens != null) {
            JsonArray places = new JsonArray();
            goldTokens.forEach((pos, count) -> {
                JsonObject data = new JsonObject();
                data.add("position", context.serialize(pos));
                data.addProperty("count", count);
                places.add(data);
            });
            tokens.add(GoldToken.GOLD.name(), places);
        }
        Map<Position, LittleBuilding> littleBuildings = root.getCapabilityModel(LittleBuildingsCapability.class);
        if (littleBuildings != null) {
            JsonArray shed = new JsonArray();
            JsonArray house = new JsonArray();
            JsonArray tower = new JsonArray();
            littleBuildings.forEach((pos, lb) -> {
                if (lb == LittleBuilding.LB_SHED) shed.add(context.serialize(pos));
                if (lb == LittleBuilding.LB_HOUSE) house.add(context.serialize(pos));
                if (lb == LittleBuilding.LB_TOWER) tower.add(context.serialize(pos));
            });
            if (shed.size() > 0) {
                tokens.add(LittleBuilding.LB_SHED.name(), shed);
            }
            if (house.size() > 0) {
                tokens.add(LittleBuilding.LB_HOUSE.name(), house);
            }
            if (tower.size() > 0) {
                tokens.add(LittleBuilding.LB_TOWER.name(), tower);
            }
        }

        return tokens;
    }

    public JsonElement serializeFeatures(GameState root, JsonSerializationContext context) {
        JsonArray features = new JsonArray();
        root.getFeatures().forEach(f -> {
            JsonObject item = new JsonObject();
            item.addProperty("type", f.getClass().getSimpleName());
            JsonArray places = new JsonArray();
            f.getPlaces().forEach(fp -> {
                JsonArray ptr = new JsonArray();
                ptr.add(fp.getPosition().x);
                ptr.add(fp.getPosition().y);
                ptr.add(fp.getLocation().toString());
                places.add(ptr);
            });
            item.add("places", places);
            if (f instanceof Tower) {
                item.addProperty("height", ((Tower) f).getHeight());
            }
            if (f instanceof Scoreable) {
                JsonArray owners = new JsonArray();
                ((Scoreable) f).getOwners(root).forEach(p -> owners.add(p.getIndex()));
                item.add("owners", owners);
            }
            if (f instanceof Field) {
                Field field = (Field) f;
                int cities = field.getAdjoiningCities().size();
                if (field.isAdjoiningCityOfCarcassonne()) {
                    cities++;
                }
                item.addProperty("cities", cities);
            }

            features.add(item);
        });
        return features;
    }

    public JsonArray serializePlayEvents(GameState root, JsonSerializationContext context) {
        JsonArray events = new JsonArray();
        int turn = 0;
        boolean volcanoTile = false;
        Player player = null;
        JsonObject item = null;
        JsonArray turnEvents = null;
        JsonArray dragonPath = null;
        for (PlayEvent ev : root.getEvents()) {
            if (ev instanceof PlayerTurnEvent) {
                player = ((PlayerTurnEvent) ev).getPlayer();
            }
            if (ev instanceof PlayerTurnEvent || ev instanceof DoubleTurnEvent)  {
                item = new JsonObject();
                turnEvents = new JsonArray();
                item.addProperty("turn", ++turn);
                item.addProperty("player", player.getIndex());
                item.add("events", turnEvents);
                events.add(item);
                // clean-up
                dragonPath = null;
                continue;
            }
            if (item == null) {
                // ignore events before first turn
                continue;
            }
            if (ev instanceof TilePlacedEvent) {
                TilePlacedEvent tev = (TilePlacedEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "tile-placed");
                data.addProperty("tile", tev.getTile().getId());
                data.add("position", context.serialize(tev.getPosition()));
                data.addProperty("rotation", rotationToPrimitive(tev.getRotation()));
                turnEvents.add(data);
                volcanoTile = tev.getTile().getTileModifiers().contains(DragonCapability.VOLCANO);
                continue;
            }
            if (ev instanceof TileDiscardedEvent) {
                TileDiscardedEvent tev = (TileDiscardedEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "tile-discarded");
                data.addProperty("tile", tev.getTile().getId());
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof ScoreEvent) {
                ScoreEvent sev = (ScoreEvent) ev;
                if (sev.isFinal() && turn != -1) {
                    turn = -1;
                    item = new JsonObject();
                    turnEvents = new JsonArray();
                    item.addProperty("finalScoring", true);
                    item.add("events", turnEvents);
                    events.add(item);
                }
                JsonObject data = new JsonObject();
                data.addProperty("type", "points");
                JsonArray points = new JsonArray();
                for (ReceivedPoints rp :  sev.getPoints()) {
                    JsonObject pts = new JsonObject();
                    pts.addProperty("player", rp.getPlayer().getIndex());
                    pts.addProperty("points", rp.getPoints());
                    pts.addProperty("name", rp.getExpression().getName());
                    JsonArray items = new JsonArray();
                    for (ExprItem exprItem : rp.getExpression().getItems()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("count", exprItem.getCount());
                        obj.addProperty("name", exprItem.getName());
                        obj.addProperty("points", exprItem.getPoints());
                        items.add(obj);
                    }
                    pts.add("items", items);
                    pts.add("ptr", context.serialize(rp.getSource()));
                    points.add(pts);
                }
                data.add("points", points);
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof MeepleDeployed) {
                MeepleDeployed mev = (MeepleDeployed) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "meeple-deployed");
                data.addProperty("meeple", mev.getMeeple().getClass().getSimpleName());
                data.addProperty("player", mev.getMeeple().getPlayer().getIndex());
                data.add("to", context.serialize(mev.getPointer()));
                if (mev.getMovedFrom() != null) {
                    data.add("from", context.serialize(mev.getMovedFrom()));
                }
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof MeepleReturned) {
                MeepleReturned mev = (MeepleReturned) ev;
                if (mev.isForced()) {
                    JsonObject data = new JsonObject();
                    data.addProperty("type", "meeple-returned");
                    data.addProperty("meeple", mev.getMeeple().getClass().getSimpleName());
                    data.addProperty("player", mev.getMeeple().getPlayer().getIndex());
                    data.add("from", context.serialize(mev.getFrom()));
                    turnEvents.add(data);
                }
                continue;
            }
            if (ev instanceof FollowerCaptured) {
                FollowerCaptured mev = (FollowerCaptured) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "meeple-captured");
                data.addProperty("meeple", mev.getFollower().getClass().getSimpleName());
                data.addProperty("player", mev.getFollower().getPlayer().getIndex());
                data.add("from", context.serialize(mev.getFrom()));
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof TokenReceivedEvent) {
                TokenReceivedEvent tev = (TokenReceivedEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "token-received");
                data.addProperty("player", tev.getPlayer().getIndex());
                data.addProperty("token", tev.getToken().name());
                data.addProperty("count", tev.getCount());
                if (tev.getSourcePositions() != null) {
                    JsonArray arr = new JsonArray();
                    tev.getSourcePositions().forEach(pos -> {
                        arr.add(context.serialize(pos));
                    });
                    data.add("positions", arr);
                } else {
                    data.add("feature", context.serialize(tev.getSourceFeature().getPlaces().get()));
                }
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof TokenPlacedEvent) {
                TokenPlacedEvent tev = (TokenPlacedEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "token-placed");
                data.addProperty("token", tev.getToken().name());
                data.add("to", context.serialize(tev.getPointer()));
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof NeutralFigureMoved) {
                if (volcanoTile) {
                    // ignore dragon move on volcano tile
                    volcanoTile = false;
                    continue;
                }
                NeutralFigureMoved nev = (NeutralFigureMoved) ev;
                if (nev.getNeutralFigure() instanceof Dragon) {
                    if (dragonPath == null) {
                        JsonObject data = new JsonObject();
                        dragonPath = new JsonArray();
                        dragonPath.add(context.serialize(nev.getFrom()));
                        dragonPath.add(context.serialize(nev.getTo()));
                        data.addProperty("type", "dragon-moved");
                        data.addProperty("figure", nev.getNeutralFigure().getId());
                        data.add("path", dragonPath);
                        turnEvents.add(data);
                    } else {
                        dragonPath.add(context.serialize(nev.getTo()));
                    }
                } else {
                    JsonObject data = new JsonObject();
                    data.addProperty("type", "neutral-moved");
                    data.addProperty("figure", nev.getNeutralFigure().getId());
                    data.add("from", context.serialize(nev.getTo()));
                    data.add("to", context.serialize(nev.getTo()));
                    turnEvents.add(data);
                }
                continue;
            }
            if (ev instanceof RansomPaidEvent) {
                RansomPaidEvent rev = (RansomPaidEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "ransom-paid");
                data.addProperty("follower", rev.getMeeple().getClass().getSimpleName());
                data.addProperty("prisoner", rev.getMeeple().getPlayer().getIndex());
                data.addProperty("jailer", rev.getJailer().getIndex());
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof PrisonersExchangeEvent) {
                PrisonersExchangeEvent xev = (PrisonersExchangeEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "prisoners-exchange");
                JsonObject first = new JsonObject();
                first.addProperty("type", xev.getFirst().getClass().getSimpleName());
                first.addProperty("player", xev.getFirst().getPlayer().getIndex());
                JsonObject second = new JsonObject();
                first.addProperty("type", xev.getSecond().getClass().getSimpleName());
                first.addProperty("player", xev.getSecond().getPlayer().getIndex());
                JsonArray exchange = new JsonArray(2);
                exchange.add(first);
                exchange.add(second);
                data.add("exchange", exchange);
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof CastleCreated) {
                CastleCreated cc = (CastleCreated) ev;
                Edge edge = cc.getCastle().getEdge();
                JsonObject data = new JsonObject();
                data.addProperty("type", "castle-created");
                JsonArray positions = new JsonArray(2);
                positions.add(context.serialize(edge.getP1()));
                positions.add(context.serialize(edge.getP2()));
                data.add("positions", positions);
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof TileAuctionedEvent) {
                TileAuctionedEvent aev = (TileAuctionedEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "tile-auctioned");
                data.addProperty("tile", aev.getTile().getId());
                data.addProperty("option", aev.getOption().name());
                data.addProperty("points", aev.getPoints());
                data.addProperty("auctioneer", aev.getAuctioneer().getIndex());
                data.addProperty("bidder", aev.getBidder() == null ? null : aev.getBidder().getIndex());
                turnEvents.add(data);
                continue;
            }
            if (ev instanceof FlierRollEvent) {
                FlierRollEvent frev = (FlierRollEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "flier-roll");
                data.addProperty("distance", frev.getDistance());
                data.add("flierPosition", context.serialize(frev.getPosition()));
                turnEvents.add(data);
                continue;
            }
        }
        return events;
    }

    private class TilePackSerializer implements JsonSerializer<TilePack> {
        @Override
        public JsonElement serialize(TilePack state, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("size", state.totalSize());
            json.addProperty("underHills", state.getHiddenUnderHills());
            return json;
        }
    }

    private class ActionsStateSerializer implements JsonSerializer<ActionsState> {
        @Override
        public JsonElement serialize(ActionsState state, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("player", state.getPlayer().getIndex());
            json.addProperty("canPass", state.isPassAllowed());

            JsonArray actions = new JsonArray(state.getActions().length());
            state.getActions().forEach(action -> {
                actions.add(context.serialize(action));
            });
            json.add("items", actions);
            return json;
        }
    }

    private class ActionSerializer implements JsonSerializer<AbstractPlayerAction<?>> {
        private String type;

        public ActionSerializer(String type) {
            this.type = type;
        }

        @Override
        public JsonElement serialize(AbstractPlayerAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", this.type);
            return json;
        }
    }

    private class SelectFeatureActionSerializer implements JsonSerializer<SelectFeatureAction> {
        @Override
        public JsonElement serialize(SelectFeatureAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", action.getClass().getSimpleName().replace("Action", ""));
            JsonArray options = new JsonArray();
            action.getOptions().forEach(fp-> {
                options.add(context.serialize(fp));
            });
            json.add("options", options);
            return json;
        }
    }

    private class TilePlacementActionSerializer implements JsonSerializer<TilePlacementAction> {
        @Override
        public JsonElement serialize(TilePlacementAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "TilePlacement");
            json.addProperty("tileId", action.getTile().getId());
            JsonArray options = new JsonArray();
            action.getOptions().groupBy(PlacementOption::getPosition).forEach((pos, group) -> {
                JsonObject opt = new JsonObject();
                opt.add("position", context.serialize(pos));
                JsonArray rotations = new JsonArray();
                group.map(PlacementOption::getRotation).toArray().sorted().forEach(rot -> { rotations.add(rotationToPrimitive(rot)); });
                opt.add("rotations", rotations);
                options.add(opt);
            });
            json.add("options", options);
            return json;
        }
    }

    private class MeepleActionSerializer implements JsonSerializer<MeepleAction> {
        @Override
        public JsonElement serialize(MeepleAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "Meeple");
            json.addProperty("meeple", action.getMeepleType().getSimpleName());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(fp -> {
                options.add(context.serialize(fp));
            });
            json.add("options", options);
            if (action.getOrigin() != null) {
                json.add("origin", context.serialize(action.getOrigin()));
            }
            return json;
        }
    }

    private class NeutralFigureActionSerializer implements JsonSerializer<NeutralFigureAction> {
        @Override
        public JsonElement serialize(NeutralFigureAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "NeutralFigure");
            json.addProperty("figureId", action.getFigure().getId());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(fp -> {
                options.add(context.serialize(fp));
            });
            json.add("options", options);
            return json;
        }
    }

    private class FerriesActionSerializer implements JsonSerializer<FerriesAction> {
        @Override
        public JsonElement serialize(FerriesAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "Ferries");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(fp -> {
                options.add(context.serialize(fp));
            });
            json.add("options", options);
            return json;
        }
    }

    private class MoveDragonActionSerializer implements JsonSerializer<MoveDragonAction> {
        @Override
        public JsonElement serialize(MoveDragonAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "MoveDragon");
            json.addProperty("figureId", action.getFigureId());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(pos -> {
                options.add(context.serialize(pos));
            });
            json.add("options", options);
            return json;
        }
    }

    private class FairyNextToActionSerializer implements JsonSerializer<FairyNextToAction> {
        @Override
        public JsonElement serialize(FairyNextToAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "MoveFairyNextTo");
            json.addProperty("figureId", action.getFigureId());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(ptr -> {
                options.add(context.serialize(ptr));
            });
            json.add("options", options);
            return json;
        }
    }

    private class FairyOnTileActionSerializer implements JsonSerializer<FairyOnTileAction> {
        @Override
        public JsonElement serialize(FairyOnTileAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "MoveFairyOnTile");
            json.addProperty("figureId", action.getFigureId());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(pos -> {
                options.add(context.serialize(pos));
            });
            json.add("options", options);
            return json;
        }
    }

    private class ReturnMeepleActionSerializer implements JsonSerializer<ReturnMeepleAction> {
        @Override
        public JsonElement serialize(ReturnMeepleAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "ReturnMeeple");
            json.addProperty("source", action.getSource().name());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(ptr -> {
                options.add(context.serialize(ptr));
            });
            json.add("options", options);
            return json;
        }
    }

    private class CaptureFollowerActionSerializer implements JsonSerializer<CaptureFollowerAction> {
        @Override
        public JsonElement serialize(CaptureFollowerAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "CaptureFollower");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(ptr -> {
                options.add(context.serialize(ptr));
            });
            json.add("options", options);
            return json;
        }
    }

    private class TowerPieceActionSerializer implements JsonSerializer<TowerPieceAction> {
        @Override
        public JsonElement serialize(TowerPieceAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "TowerPiece");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(pos -> {
                options.add(context.serialize(pos));
            });
            json.add("options", options);
            return json;
        }
    }


    private class SelectPrisonerToExchangeActionSerializer implements  JsonSerializer<SelectPrisonerToExchangeAction> {
        @Override
        public JsonElement serialize(SelectPrisonerToExchangeAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "SelectPrisonerToExchange");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(f -> {
                JsonObject item = new JsonObject();
                item.addProperty("type", f.getClass().getSimpleName());
                item.addProperty("id", f.getId());
                options.add(item);
            });
            json.add("options", options);
            return json;
        }
    }

    private class CornCircleSelectDeployOrRemoveActionSerializer implements JsonSerializer<CornCircleSelectDeployOrRemoveAction> {
        @Override
        public JsonElement serialize(CornCircleSelectDeployOrRemoveAction action, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "CornCircleSelectDeployOrRemove");
            json.addProperty("featureType", action.getCornType().getSimpleName());
            return json;
        }
    }

    private class FlockActionSerializer implements JsonSerializer<FlockAction> {
        @Override
        public JsonElement serialize(FlockAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "Flock");
            json.addProperty("meepleId", action.getShepherdPointer().getMeepleId());
            json.add("position", context.serialize(action.getShepherdPointer().getPosition()));
            json.addProperty("feature", "Field");
            json.add("location", context.serialize(action.getShepherdPointer().getLocation()));
            return json;
        }
    }

    private class TunnelActionSerializer implements JsonSerializer<TunnelAction> {
        @Override
        public JsonElement serialize(TunnelAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "Tunnel");
            json.addProperty("token", action.getToken().name());
            JsonArray options = new JsonArray();
            action.getOptions().forEach(fp -> {
                options.add(context.serialize(fp));
            });
            json.add("options", options);
            return json;
        }
    }

    private class GoldPieceActionSerializer implements JsonSerializer<GoldPieceAction> {
        @Override
        public JsonElement serialize(GoldPieceAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "GoldPiece");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(ptr -> {
                options.add(context.serialize(ptr));
            });
            json.add("options", options);
            return json;
        }
    }

    private class LittleBuildingActionSerializer implements JsonSerializer<LittleBuildingAction> {
        @Override
        public JsonElement serialize(LittleBuildingAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "LittleBuilding");
            JsonArray options = new JsonArray();
            action.getOptions().forEach(lb -> {
                options.add(lb.name());
            });
            json.add("options", options);
            json.add("position", context.serialize(action.getPosition()));
            return json;
        }
    }
}
