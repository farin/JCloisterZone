package com.jcloisterzone.event.setup;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.Event;

public class ExpansionChangedEvent extends Event {

    private final Expansion expansion;
    private final int count;

    public ExpansionChangedEvent(Expansion expansion, int count) {
        this.expansion = expansion;
        this.count = count;
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return super.toString() + " " + expansion.name() + " " + count;
    }
}
