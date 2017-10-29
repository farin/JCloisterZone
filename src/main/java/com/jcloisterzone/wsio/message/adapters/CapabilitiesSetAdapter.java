package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Capability;

public class CapabilitiesSetAdapter extends TypeAdapter<Set<Class<? extends Capability<?>>>> {

    private CapabilitiesAdapter capabilityAdapter = new CapabilitiesAdapter();

    @Override
    public void write(JsonWriter out, Set<Class<? extends Capability<?>>> value) throws IOException {
        out.beginArray();
        for (Class<? extends Capability<?>> cls : value) {
            capabilityAdapter.write(out, cls);
        }
        out.endArray();
    }

    @Override
    public Set<Class<? extends Capability<?>>> read(JsonReader in) throws IOException {
        Set<Class<? extends Capability<?>>> result = new HashSet<>();
        in.beginArray();
        while (in.hasNext()) {
            result.add(capabilityAdapter.read(in));
        }
        in.endArray();
        return result;
    }

}