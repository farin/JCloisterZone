package com.jcloisterzone.wsio;

import java.lang.reflect.Type;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;

public final class MessageParser {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

        builder.registerTypeAdapter(SetRuleMessage.class, new JsonDeserializer<SetRuleMessage>() {
            @Override
            public SetRuleMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                Rule rule = Rule.valueOf(obj.get("rule").getAsString());
                SetRuleMessage msg = new SetRuleMessage(
                    rule,
                    !obj.has("value") || obj.get("value") instanceof JsonNull ? null : rule.unpackValue(obj.get("value").getAsString())
                );
                msg.setGameId(obj.get("gameId").getAsString());
                return msg;
            }
        });
        builder.registerTypeAdapter(Expansion.class, new JsonSerializer<Expansion>() {
            @Override
            public JsonElement serialize(Expansion src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.name());
            }
        });
        builder.registerTypeAdapter(Expansion.class, new JsonDeserializer<Expansion>() {
            @Override
            public Expansion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return Expansion.valueOf(json.getAsString());
            }
        });
        builder.registerTypeAdapter(Position.class, new PositionSerializer());
        builder.registerTypeAdapter(Position.class, new JsonDeserializer<Position>() {
            @Override
            public Position deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonArray arr = json.getAsJsonArray();
                return new Position(arr.get(0).getAsInt(), arr.get(1).getAsInt());
            }
        });
        builder.registerTypeAdapter(Location.class, new LocationSerializer());
        builder.registerTypeAdapter(Location.class, new JsonDeserializer<Location>() {
            @Override
            public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return Location.valueOf(json.getAsString());
            }
        });
        builder.registerTypeAdapter(BoardPointer.class, new BoardPointerSerializer());
        builder.registerTypeAdapter(BoardPointer.class, new JsonDeserializer<BoardPointer>() {
            @Override
            public BoardPointer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                if (json.isJsonArray()) {
                    return context.deserialize(json, Position.class);
                }
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("meepleId")) {
                    return context.deserialize(json, MeeplePointer.class);
                }
                if (obj.has("location")) {
                    return context.deserialize(json, FeaturePointer.class);
                }
                return context.deserialize(json, Position.class);
            }
        });

        JsonSerializer<WsMessage> msgSerializer = new JsonSerializer<WsMessage>() {
            @Override
            public JsonElement serialize(WsMessage src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject obj = new JsonObject();
                obj.add("type", new JsonPrimitive(src.getClass().getAnnotation(WsMessageCommand.class).value()));
                obj.add("payload", context.serialize(src));
                return obj;
            }
        };
        JsonDeserializer<WsMessage> msgDeserializer = new JsonDeserializer<WsMessage>() {
            @Override
            public WsMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    JsonObject obj = (JsonObject) json;
                    Class<? extends WsMessage> cls = WsCommandRegistry.TYPES.get(obj.get("type").getAsString()).get();
                    WsMessage msg = context.deserialize(obj.get("payload"), cls);
                    JsonElement msgId = obj.get("payload").getAsJsonObject().get("messageId");
                    if (msgId != null) {
                        msg.setMessageId(msgId.getAsString());
                    }
                    return msg;
                } catch (RuntimeException e) {
                    System.err.println(json);
                    System.err.println(e);
                    throw e;
                }
            }
        };

        builder.registerTypeAdapter(WsMessage.class, msgSerializer);
        builder.registerTypeAdapter(WsMessage.class, msgDeserializer);
        builder.registerTypeAdapter(WsReplayableMessage.class, msgSerializer);
        builder.registerTypeAdapter(WsReplayableMessage.class, msgDeserializer);

        return builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public MessageParser() {
        gson = createGsonBuilder().create();
    }

    protected String getCmdName(Class<? extends WsMessage> msgType) {
        return msgType.getAnnotation(WsMessageCommand.class).value();
    }

    public WsMessage fromJson(String src) {
//        JsonParser parser = new JsonParser();
//        JsonObject obj = parser.parse(src).getAsJsonObject();
//        String typeName = obj.get("type").getAsString();
//        Class<? extends WsMessage> type = WsCommandRegistry.TYPES.get(typeName).getOrElseThrow(
//            () -> new IllegalArgumentException("Mapping type is not declared for " + typeName)
//        );
        return gson.fromJson(src, WsMessage.class);
    }

    public String toJson(WsMessage arg) {
        return gson.toJson(arg, WsMessage.class);
    }

    public Gson getGson() {
        return gson;
    }

    public static class PositionSerializer implements JsonSerializer<Position> {
        @Override
        public JsonElement serialize(Position src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray(2);
            arr.add(src.x);
            arr.add(src.y);
            return arr;
        }
    }

    public static class LocationSerializer implements JsonSerializer<Location> {
        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class BoardPointerSerializer implements JsonSerializer<BoardPointer> {
        @Override
        public JsonElement serialize(BoardPointer src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src);
        }
    }
}
