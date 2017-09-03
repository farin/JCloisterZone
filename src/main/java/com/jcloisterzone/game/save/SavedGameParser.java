package com.jcloisterzone.game.save;

import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.WsCommandRegistry;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;

public class SavedGameParser {

    private Gson gson;

    public SavedGameParser(Config config) {
        GsonBuilder builder = MessageParser.createGsonBuilder();

         builder
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setExclusionStrategies(new SavedGameExclStrat());

         if ("pretty".equals(config.getSaved_games().getFormat())) {
            builder.setPrettyPrinting();
         }

         gson = builder.create();
    }

    public String toJson(SavedGame src) {
        return gson.toJson(src);
    }

    public void toJson(SavedGame src, Writer writer) {
        gson.toJson(src, writer);
    }

    public SavedGame fromJson(JsonReader reader) {
        SavedGame sg = gson.fromJson(reader, SavedGame.class);
        sg.getReplay().forEach(msg -> msg.setGameId(sg.getGameId()));
        return sg;
    }

    public class SavedGameExclStrat implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return (WsReplayableMessage.class.isAssignableFrom(f.getDeclaringClass()) && f.getName().equals("gameId"));
        }

    }

}
