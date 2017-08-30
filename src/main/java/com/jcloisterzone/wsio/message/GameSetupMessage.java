package com.jcloisterzone.wsio.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME_SETUP")
public class GameSetupMessage implements WsMessage, WsInGameMessage	 {
    private String gameId;
    @JsonAdapter(CustomRulesMapAdapter.class)
    private Map<CustomRule, Object> rules;
    private Set<Expansion> expansions;
    //private Set<Class<? extends Capability<?>>> capabilityClasses;

    public GameSetupMessage() {
    }

    public GameSetupMessage(Map<CustomRule, Object> rules, Set<Expansion> expansions
            /*Set<Class<? extends Capability<?>>> capabilityClasses*/) {
        this.rules = rules;
        this.expansions = expansions;
        //this.capabilityClasses = capabilityClasses;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Map<CustomRule, Object> getRules() {
        return rules;
    }

    public void setRules(Map<CustomRule, Object> rules) {
        this.rules = rules;
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public void setExpansions(Set<Expansion> expansions) {
        this.expansions = expansions;
    }

//    public Set<Class<? extends Capability<?>>> getCapabilityClasses() {
//        return capabilityClasses;
//    }
//
//    public void setCapabilityClasses(Set<Class<? extends Capability<?>>> capabilityClasses) {
//        this.capabilityClasses = capabilityClasses;
//    }

    public static class CustomRulesMapAdapter extends TypeAdapter<Map<CustomRule, Object>> {

        @Override
        public void write(JsonWriter out, Map<CustomRule, Object> value) throws IOException {
            out.beginObject();
            for (Entry<CustomRule, Object> entry : value.entrySet()) {
                out.name(entry.getKey().name());
                if (entry.getValue() instanceof Boolean) {
                    out.value((Boolean)entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                    out.value((Integer)entry.getValue());
                } else {
                    out.value(entry.getValue().toString());
                }
            }
            out.endObject();
        }

        @Override
        public Map<CustomRule, Object> read(JsonReader in) throws IOException {
            Map<CustomRule, Object> result = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                CustomRule rule = CustomRule.valueOf(in.nextName());
                JsonToken p = in.peek();
                if (p == JsonToken.BOOLEAN) {
                    result.put(rule, in.nextBoolean());
                } else if (p == JsonToken.NUMBER) {
                    result.put(rule, in.nextInt());
                } else {
                    result.put(rule, rule.unpackValue(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    }

}
