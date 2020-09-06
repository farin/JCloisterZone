package com.jcloisterzone.event;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;

public class FollowerCaptured extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private Follower follower;
    private FeaturePointer from;

    public FollowerCaptured(PlayEventMeta metadata, Follower follower, FeaturePointer from) {
        super(metadata);
        this.follower = follower;
        this.from = from;
    }

    public Meeple getFollower() {
        return follower;
    }

    public BoardPointer getFrom() {
        return from;
    }

}
