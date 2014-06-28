package com.jcloisterzone.event.setup;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.Event;

public class ExpansionChangedEvent extends Event {

    private final Expansion expansion;
    private final boolean enabled;

    public ExpansionChangedEvent(Expansion expansion, boolean enabled) {
        this.expansion = expansion;
        this.enabled = enabled;
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
