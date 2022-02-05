package com.jcloisterzone.io;

import com.google.gson.*;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.ReplayableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public final class MessageParser {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

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
        builder.registerTypeAdapter(FeaturePointer.class, new FeaturePointerSerializer());
        builder.registerTypeAdapter(FeaturePointer.class, new JsonDeserializer<FeaturePointer>() {
            @Override
            public FeaturePointer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = (JsonObject) json;
                Position pos = context.deserialize(obj.get("position"), Position.class);
                Location loc = context.deserialize(obj.get("location"), Location.class);
                String featureType = obj.get("feature").getAsString();
                Class<? extends Feature> feature = null;
                // TOOD use annotation ?
                switch (featureType) {
                    case "City": feature = City.class; break;
                    case "Road": feature = Road.class; break;
                    case "Field": feature = Field.class; break;
                    case "Monastery": feature = Monastery.class; break;
                    case "Garden": feature = Garden.class; break;
                    case "Tower": feature = Tower.class; break;
                    case "Quarter": feature = Quarter.class; break;
                    case "YagaHut": feature = YagaHut.class; break;
                    case "Vodyanoy": feature = Vodyanoy.class; break;
                    case "SoloveiRazboynik": feature = SoloveiRazboynik.class; break;
                    case "FlyingMachine": feature = FlyingMachine.class; break;
                    case "Castle": feature = Castle.class; break;
                    case "Acrobats": feature = Acrobats.class; break;
                    case "River": feature = River.class; break;
                }
                return new FeaturePointer(pos, feature, loc);
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

        JsonSerializer<Message> msgSerializer = new JsonSerializer<Message>() {
            @Override
            public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject obj = new JsonObject();
                obj.add("type", new JsonPrimitive(src.getClass().getAnnotation(MessageCommand.class).value()));
                obj.add("payload", context.serialize(src));
                return obj;
            }
        };
        JsonDeserializer<Message> msgDeserializer = new JsonDeserializer<Message>() {
            @Override
            public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    JsonObject obj = (JsonObject) json;
                    Class<? extends Message> cls = CommandRegistry.TYPES.get(obj.get("type").getAsString()).get();
                    return context.deserialize(obj.get("payload"), cls);
                } catch (RuntimeException e) {
                    System.err.println(json);
                    System.err.println(e);
                    throw e;
                }
            }
        };

        builder.registerTypeAdapter(Message.class, msgSerializer);
        builder.registerTypeAdapter(Message.class, msgDeserializer);
        builder.registerTypeAdapter(ReplayableMessage.class, msgSerializer);
        builder.registerTypeAdapter(ReplayableMessage.class, msgDeserializer);

        return builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public MessageParser() {
        gson = createGsonBuilder().create();
    }

    protected String getCmdName(Class<? extends Message> msgType) {
        return msgType.getAnnotation(MessageCommand.class).value();
    }

    public Message fromJson(String src) {
        return gson.fromJson(src, Message.class);
    }

    public String toJson(Message arg) {
        return gson.toJson(arg, Message.class);
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

    public static class FeaturePointerSerializer implements JsonSerializer<FeaturePointer> {
        @Override
        public JsonElement serialize(FeaturePointer fp, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("position", context.serialize(fp.getPosition()));
            obj.add("location", context.serialize(fp.getLocation()));
            obj.addProperty("feature", fp.getFeature() != null ? fp.getFeature().getSimpleName() : null);
            return obj;
        }
    }

    public static class BoardPointerSerializer implements JsonSerializer<BoardPointer> {
        @Override
        public JsonElement serialize(BoardPointer src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src);
        }
    }
}
