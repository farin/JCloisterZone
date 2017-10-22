package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Capability;

public class CapabilitiesAdapter extends TypeAdapter<Class<? extends Capability<?>>> {

    @Override
    public void write(JsonWriter out, Class<? extends Capability<?>> value) throws IOException {
        out.value(Capability.nameForClass(value));
    }

    @Override
    public Class<? extends Capability<?>> read(JsonReader in) throws IOException {
        try {
			return Capability.classForName(in.nextString());
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
    }

}