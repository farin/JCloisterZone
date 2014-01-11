package com.jcloisterzone.event.setup;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.Event;

public class SupportedExpansionsChangeEvent extends Event {

    private final EnumSet<Expansion> expansions;

    public SupportedExpansionsChangeEvent(EnumSet<Expansion> expansions) {
        super();
        this.expansions = expansions;
    }

    public EnumSet<Expansion> getExpansions() {
        return expansions;
    }
}
