package com.jcloisterzone.engine;

import com.google.gson.*;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.*;
import com.jcloisterzone.board.*;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.*;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.phase.DragonMovePhase;
import com.jcloisterzone.game.state.*;
import com.jcloisterzone.wsio.MessageParser;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Vector;

import java.lang.reflect.Type;

public class StateGsonBuilder {

    public Gson create() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Game.class, new GameSerializer());
        builder.registerTypeAdapter(TilePack.class, new TilePackSerializer());
        builder.registerTypeAdapter(ActionsState.class, new ActionsStateSerializer());
        builder.registerTypeAdapter(Position.class, new MessageParser.PositionSerializer());
        builder.registerTypeAdapter(Location.class, new MessageParser.LocationSerializer());
        builder.registerTypeAdapter(BoardPointer.class, new MessageParser.BoardPointerSerializer());
        // actions
        builder.registerTypeAdapter(TilePlacementAction.class, new TilePlacementActionSerializer());
        builder.registerTypeAdapter(MeepleAction.class, new MeepleActionSerializer());
        builder.registerTypeAdapter(ReturnMeepleAction.class, new ReturnMeepleActionSerializer());
        builder.registerTypeAdapter(MoveDragonAction.class, new MoveDragonActionSerializer());
        builder.registerTypeAdapter(FairyNextToAction.class, new FairyNextToActionSerializer());
        builder.registerTypeAdapter(FairyOnTileAction.class, new FairyOnTileActionSerializer());
        builder.registerTypeAdapter(TowerPieceAction.class, new TowerPieceActionSerializer());
        builder.registerTypeAdapter(CaptureFollowerAction.class, new CaptureFollowerActionSerializer());
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
            obj.add("features", serializeFeatures(state, context));
            obj.addProperty("phase", state.getPhase().getSimpleName());
            obj.add("action", context.serialize(state.getPlayerActions()));
            obj.add("history", serializePlayEvents(state, context));
            obj.addProperty("undo", game.isUndoAllowed());
            return obj;
        }
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
        int playerCount = state.getPlayers().length();
        JsonArray players = new JsonArray(playerCount);
        for (int i = 0; i < playerCount; i++) {
            JsonObject player = new JsonObject();
            player.addProperty("points", state.getScore().get(i).getPoints());

            JsonObject tokens = new JsonObject();
            state.getTokens().get(i).forEach((token, count) -> {
                tokens.addProperty(token.name(), count);
            });
            player.add("tokens", tokens);

            JsonObject meeples = new JsonObject();
            state.getFollowers().get(i).filter(f -> f.isInSupply(root)).groupBy(f -> f.getClass()).forEach((cls, arr) -> {
                //followers.addProperty(cls.getSimpleName(), arr.size());
                JsonArray sizeAndId = new JsonArray(2);
                sizeAndId.add(arr.size());
                sizeAndId.add(arr.get().getId());
                meeples.add(cls.getSimpleName(), sizeAndId);
            });
            state.getSpecialMeeples().get(i).filter(f -> f.isInSupply(root)).groupBy(f -> f.getClass()).forEach((cls, arr) -> {
                //specialMeeples.addProperty(cls.getSimpleName(), arr.size());
                JsonArray sizeAndId = new JsonArray(2);
                sizeAndId.add(arr.size());
                sizeAndId.add(arr.get().getId());
                meeples.add(cls.getSimpleName(), sizeAndId);
            });
            player.add("meeples", meeples);

            if (state.getTurnPlayerIndex() == i) {
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
            if (root.getPhase().equals(DragonMovePhase.class)) {
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
        return neutral;
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
            features.add(item);
        });
        return features;
    }

    public JsonArray serializePlayEvents(GameState root, JsonSerializationContext context) {
        JsonArray events = new JsonArray();
        int turn = 0;
        Player player = null;
        JsonObject item = null;
        JsonArray turnEvents = null;
        JsonArray scoreEvents = null;
        for (PlayEvent ev : root.getEvents()) {
            if (ev instanceof PlayerTurnEvent) {
                player = ((PlayerTurnEvent) ev).getPlayer();
            }
            if (ev instanceof PlayerTurnEvent || ev instanceof DoubleTurnEvent)  {
                item = new JsonObject();
                turnEvents = new JsonArray();
                scoreEvents = new JsonArray();
                item.addProperty("turn", ++turn);
                item.addProperty("player", player.getIndex());
                item.add("events", turnEvents);
                item.add("score", scoreEvents);
                events.add(item);
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
                continue;
            }
            if (ev instanceof ScoreEvent) {
                ScoreEvent sev = (ScoreEvent) ev;
                JsonObject data = new JsonObject();
                data.addProperty("player", sev.getReceiver().getIndex());
                data.addProperty("points", sev.getPoints());
                // TODO source
                scoreEvents.add(data);
                continue;
            }
            if (ev instanceof MeepleDeployed) {
                MeepleDeployed mev = (MeepleDeployed) ev;
                JsonObject data = new JsonObject();
                data.addProperty("type", "meeple-deployed");
                data.addProperty("meeple", mev.getMeeple().getClass().getSimpleName());
                data.add("to", context.serialize(mev.getPointer()));
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
                // TODO sorted
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
}
