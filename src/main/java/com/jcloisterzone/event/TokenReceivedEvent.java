package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Token;

import io.vavr.collection.Vector;

public class TokenReceivedEvent extends PlayEvent {

    private final Token token;
    private final Player player;
    private final int count;

    // feature or position is filled
    private Feature sourceFeature;
    private Vector<Position> sourcePositions;

    public TokenReceivedEvent(PlayEventMeta metadata, Player player, Token token, int count) {
        super(metadata);
        this.token = token;
        this.player = player;
        this.count = count;
    }

    public Token getToken() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCount() {
        return count;
    }

    public Feature getSourceFeature() {
        return sourceFeature;
    }

    public void setSourceFeature(Feature sourceFeature) {
        this.sourceFeature = sourceFeature;
    }

    public Vector<Position> getSourcePositions() {
        return sourcePositions;
    }

    public void setSourcePositions(Vector<Position> sourcePositions) {
        this.sourcePositions = sourcePositions;
    }
}
