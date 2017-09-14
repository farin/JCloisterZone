package com.jcloisterzone.event.setup;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.Capability;

public class CapabilityChangeEvent extends Event {

    private final Class<? extends Capability<?>> capability;
    private final boolean enabled;

    public CapabilityChangeEvent(Class<? extends Capability<?>> capability, boolean enabled) {
        this.capability = capability;
        this.enabled = enabled;
    }

    public Class<? extends Capability<?>> getCapability() {
        return capability;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
