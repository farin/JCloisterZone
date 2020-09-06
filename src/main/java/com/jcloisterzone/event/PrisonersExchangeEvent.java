package com.jcloisterzone.event;

import com.jcloisterzone.figure.Follower;

public class PrisonersExchangeEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Follower first;
    private final Follower second;

    public PrisonersExchangeEvent(PlayEventMeta metadata, Follower first, Follower second) {
        super(metadata);
        this.first = first;
        this.second = second;
    }

    public Follower getFirst() {
        return first;
    }

    public Follower getSecond() {
        return second;
    }
}
