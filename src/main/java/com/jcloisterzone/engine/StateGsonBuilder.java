package com.jcloisterzone.engine;

import com.google.gson.*;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.*;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.wsio.MessageParser;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;

import java.lang.reflect.Type;

public class StateGsonBuilder {

    public Gson create() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GameState.class, new GameStateSerializer());
        builder.registerTypeAdapter(PlayersState.class, new PlayersStateSerializer());
        builder.registerTypeAdapter(TilePack.class, new TilePackSerializer());
        builder.registerTypeAdapter(ActionsState.class, new ActionsStateSerializer());
        builder.registerTypeAdapter(Position.class, new MessageParser.PositionSerializer());
        builder.registerTypeAdapter(Location.class, new MessageParser.LocationSerializer());
        // actions
        builder.registerTypeAdapter(TilePlacementAction.class, new TilePlacementActionSerializer());

        return builder.create();
    }

    private String rotationToPrimitive(Rotation rot) {
        // TODO sent as svg rotation
        return rot.name();
    }

    private class GameStateSerializer implements JsonSerializer<GameState> {
        public JsonElement serialize(GameState state, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("players", context.serialize(state.getPlayers()));
            obj.add("tilePack", context.serialize(state.getTilePack()));
            obj.add("placedTiles", serializePlacedTiles(state.getPlacedTiles(), context));
            obj.add("discardedTiles", serializeDiscardedTiles(state.getDiscardedTiles(), context));
            obj.add("actions", context.serialize(state.getPlayerActions()));
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

    private class PlayersStateSerializer implements JsonSerializer<PlayersState> {
        @Override
        public JsonElement serialize(PlayersState state, Type typeOfSrc, JsonSerializationContext context) {
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

                JsonObject followers = new JsonObject();
                state.getFollowers().get(i).forEach(f -> {
                    followers.addProperty(f.getId(), f.getClass().getSimpleName());
                });
                player.add("followers", followers);

                JsonObject specialMeeples = new JsonObject();
                state.getSpecialMeeples().get(i).forEach(f -> {
                    specialMeeples.addProperty(f.getId(), f.getClass().getSimpleName());
                });
                player.add("meeples", specialMeeples);

                if (state.getTurnPlayerIndex() == i) {
                    player.addProperty("turn", true);
                }
                players.add(player);
            }
            return players;
        }
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
            json.add("actions", actions);
            return json;
        }
    }

    private class TilePlacementActionSerializer implements JsonSerializer<TilePlacementAction> {
        @Override
        public JsonElement serialize(TilePlacementAction action, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "TilePlacement");
            json.addProperty("tile", action.getTile().getId());
            JsonArray options = new JsonArray();
            action.getOptions().groupBy(PlacementOption::getPosition).forEach((pos, placedTiles) -> {
                JsonObject opt = new JsonObject();
                opt.add("position", context.serialize(pos));
                JsonArray rotations = new JsonArray();
                // TODO sorted
                placedTiles.forEach(pt -> { rotations.add(rotationToPrimitive(pt.getRotation())); });
                opt.add("rotations", rotations);
                options.add(opt);
            });
            json.add("options", options);
            return json;
        }
    }
}
