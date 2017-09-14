package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Rule;

public class RulesMapAdapter extends TypeAdapter<Map<Rule, Object>> {

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