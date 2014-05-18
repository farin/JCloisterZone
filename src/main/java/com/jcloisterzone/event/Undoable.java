package com.jcloisterzone.event;

import com.jcloisterzone.game.Game;

public interface Undoable {

    public void undo(Game game);
}
