package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.Expansion;

public class ExpansionMapAdapter extends TypeAdapter<Map<Expansion, Integer>> {

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
            result.put(exp, in.nextInt());
        }
        in.endObject();
        return result;
    }
}