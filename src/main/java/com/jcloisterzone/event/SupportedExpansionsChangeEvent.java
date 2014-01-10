package com.jcloisterzone.event;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;

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
