package com.jcloisterzone.wsio.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME_SETUP")
public class GameSetupMessage implements WsMessage, WsInGameMessage	 {
    private String gameId;
    @JsonAdapter(RulesMapAdapter.class)
    private Map<Rule, Object> rules;
    @JsonAdapter(ExpansionMapAdapter.class)
    private Map<Expansion, Integer> expansions;
    //private Set<Class<? extends Capability<?>>> capabilityClasses;

    public GameSetupMessage() {
    }

    public GameSetupMessage(Map<Rule, Object> rules, Map<Expansion, Integer> expansions
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

    public Map<Rule, Object> getRules() {
        return rules;
    }

    public void setRules(Map<Rule, Object> rules) {
        this.rules = rules;
    }

    public Map<Expansion, Integer> getExpansions() {
        return expansions;
    }

    public void setExpansions(Map<Expansion, Integer> expansions) {
        this.expansions = expansions;
    }

//    public Set<Class<? extends Capability<?>>> getCapabilityClasses() {
//        return capabilityClasses;
//    }
//
//    public void setCapabilityClasses(Set<Class<? extends Capability<?>>> capabilityClasses) {
//        this.capabilityClasses = capabilityClasses;
//    }

    public static class RulesMapAdapter extends TypeAdapter<Map<Rule, Object>> {

        @Override
        public void write(JsonWriter out, Map<Rule, Object> value) throws IOException {
            out.beginObject();
            for (Entry<Rule, Object> entry : value.entrySet()) {
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
        public Map<Rule, Object> read(JsonReader in) throws IOException {
            Map<Rule, Object> result = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                Rule rule = Rule.valueOf(in.nextName());
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

    public static class ExpansionMapAdapter extends TypeAdapter<Map<Expansion, Integer>> {

        @Override
        public void write(JsonWriter out, Map<Expansion, Integer> value) throws IOException {
            out.beginObject();
            for (Entry<Expansion, Integer> entry : value.entrySet()) {
                out.name(entry.getKey().name());
                out.value(entry.getValue().intValue());
            }
            out.endObject();
        }

        @Override
        public Map<Expansion, Integer> read(JsonReader in) throws IOException {
            Map<Expansion, Integer> result = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                Expansion exp = Expansion.valueOf(in.nextName());
                JsonToken p = in.peek();
                result.put(exp, in.nextInt());
            }
            in.endObject();
            return result;
        }
    }

}
