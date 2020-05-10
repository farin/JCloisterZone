package com.jcloisterzone.engine;

import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.UndoHistoryItem;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import io.vavr.Tuple2;
import io.vavr.collection.List;

public class Game {
    private GameSetup gameSetup;
    private GameState state;
    private List<WsReplayableMessage> replay;
    private List<UndoHistoryItem> undoHistory = List.empty();

    public Game(GameSetup gameSetup) {
        this.gameSetup = gameSetup;
        replay = List.empty();
    }

    public void replaceState(GameState state) {
        this.state = state;
    }

    public void markUndo() {
        undoHistory = undoHistory.prepend(new UndoHistoryItem(state, replay));
    }

    public void clearUndo() {
        undoHistory = List.empty();
    }

    public boolean isUndoAllowed() {
        return !undoHistory.isEmpty();
    }

    public void undo() {
        if (undoHistory.isEmpty()) {
            throw new IllegalStateException();
        }
        Tuple2<UndoHistoryItem, List<UndoHistoryItem>> head = undoHistory.pop2();
        undoHistory = head._2;
        replay = head._1.getReplay();
        // note: seed should be unchanged for current usage
        // when seed is changed undo history is cleared
        replaceState(head._1.getState());
    }

    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    public void setReplay(List<WsReplayableMessage> replay) {
        this.replay = replay;
    }

    public GameState getState() {
        return state;
    }
}
