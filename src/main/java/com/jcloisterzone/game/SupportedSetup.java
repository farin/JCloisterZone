package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesSetAdapter;

public class SupportedSetup implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonAdapter(CapabilitiesSetAdapter.class)
    private Set<Class<? extends Capability<?>>> capabilities;
    private Set<Expansion> tiles;

    public static SupportedSetup getCurrentClientSupported() {
        Set<Class<? extends Capability<?>>> capabilities = new HashSet<>();
        Set<Expansion> tiles = Expansion.values().toJavaSet();
        for (Expansion exp : tiles) {
            for (Class<? extends Capability<?>> cap : exp.getCapabilities()) {
                capabilities.add(cap);
            }
        }
        return new SupportedSetup(capabilities, tiles);
    }

    public SupportedSetup(Set<Class<? extends Capability<?>>> capabilities, Set<Expansion> tiles) {
        super();
        this.capabilities = capabilities;
        this.tiles = tiles;
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<Expansion> getTiles() {
        return tiles;
    }

    public void setTiles(Set<Expansion> tiles) {
        this.tiles = tiles;
    }

    public SupportedSetup intersect(SupportedSetup other) {
        Set<Class<? extends Capability<?>>> capabilities = new HashSet<>(this.capabilities);
        capabilities.retainAll(other.capabilities);
        Set<Expansion> tiles = new HashSet<>(this.tiles);
        tiles.retainAll(other.tiles);
        return new SupportedSetup(capabilities, tiles);
    }
}
