package com.jcloisterzone.event.setup;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.Event;

public class SupportedExpansionsChangeEvent extends Event {

    private final Set<Expansion> expansions;

    public SupportedExpansionsChangeEvent(Set<Expansion> expansions) {
        super();
        this.expansions = expansions;
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }
}
