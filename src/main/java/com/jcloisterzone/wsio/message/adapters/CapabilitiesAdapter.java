package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Capability;

public class CapabilitiesAdapter extends TypeAdapter<Class<? extends Capability<?>>> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public Class<? extends Capability<?>> classForName(String name) {
        String clsName = "com.jcloisterzone.game.capability." + name + "Capability";
        try {
            return (Class<? extends Capability<?>>) Class.forName(clsName);
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find capability class.", e);
            return null;
        }
    }

    public String nameForClass(Class<? extends Capability<?>> cls) {
        return cls.getSimpleName().replaceAll("Capability$", "");
    }

    @Override
    public void write(JsonWriter out, Class<? extends Capability<?>> value) throws IOException {
        out.value(nameForClass(value));
    }

    @Override
    public Class<? extends Capability<?>> read(JsonReader in) throws IOException {
        return classForName(in.nextString());
    }

}