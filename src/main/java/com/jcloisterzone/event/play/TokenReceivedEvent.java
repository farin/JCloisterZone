package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Token;

public class TokenReceivedEvent extends PlayEvent {

    private final Token token;
    private final Player player;
    private final int count;

    // feature or position is filled
    private Feature sourceFeature;
    private Position sourcePosition;

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

    public Position getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(Position sourcePosition) {
        this.sourcePosition = sourcePosition;
    }
}
