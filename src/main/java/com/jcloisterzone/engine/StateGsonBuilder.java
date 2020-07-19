package com.jcloisterzone.engine;

import com.google.gson.*;
import com.jcloisterzone.action.*;
import com.jcloisterzone.board.*;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.*;
import com.jcloisterzone.wsio.MessageParser;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;

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
            obj.addProperty("phase", state.getPhase().getSimpleName());
            obj.add("action", context.serialize(state.getPlayerActions()));
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
            neutral.add("dragon", context.serialize(pos));
        }
        BoardPointer bptr = state.getFairyDeployment();
        if (bptr != null) {
            neutral.add("fairy", context.serialize(bptr));
        }
        FeaturePointer fp = state.getMageDeployment();
        if (fp != null) {
            neutral.add("mage", context.serialize(fp));
        }
        fp = state.getWitchDeployment();
        if (fp != null) {
            neutral.add("witch", context.serialize(fp));
        }
        fp = state.getCountDeployment();
        if (fp != null) {
            neutral.add("count", context.serialize(fp));
        }
        return neutral;

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
}
