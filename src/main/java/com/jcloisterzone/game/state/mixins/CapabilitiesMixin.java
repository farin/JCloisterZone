package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;

import java.util.function.Function;

public interface CapabilitiesMixin {

    CapabilitiesState getCapabilities();
    GameState setCapabilities(CapabilitiesState capabilities);

    default boolean hasCapability(Class<? extends Capability<?>> cls) {
        return getCapabilities().contains(cls);
    }

    default <M> M getCapabilityModel(Class<? extends Capability<M>> cls) {
        return getCapabilities().getModel(cls);
    }

    default <M> GameState setCapabilityModel(Class<? extends Capability<M>> cls, M model) {
        return setCapabilities(getCapabilities().setModel(cls, model));
    }

    default <M> GameState mapCapabilityModel(Class<? extends Capability<M>> cls, Function<M, M> fn) {
        return setCapabilities(getCapabilities().updateModel(cls, fn));
    }
}
