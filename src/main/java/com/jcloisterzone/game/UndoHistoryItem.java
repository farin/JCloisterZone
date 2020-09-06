package com.jcloisterzone.game;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ReplayableMessage;
import io.vavr.collection.List;

import java.io.Serializable;

@Immutable
public class UndoHistoryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GameState state;
    private final List<ReplayableMessage> replay;

    public UndoHistoryItem(GameState state, List<ReplayableMessage> replay) {
        this.state = state;
        this.replay = replay;
    }

    public GameState getState() {
        return state;
    }

    public List<ReplayableMessage> getReplay() {
        return replay;
    }

    @Override
    public String toString() {
    	return "UndoHistoryItem(" + replay.head() + ")";
    }
}
