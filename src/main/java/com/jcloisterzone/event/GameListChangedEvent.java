package com.jcloisterzone.event;

import java.util.Arrays;

import com.jcloisterzone.ui.GameController;

public class GameListChangedEvent extends Event {

    private GameController[] gameControllers;

    public GameListChangedEvent(GameController[] gameControllers) {
        super();
        this.gameControllers = gameControllers;
    }

	public GameController[] getGameControllers() {
		return gameControllers;
	}

	public void setGameControllers(GameController[] gameControllers) {
		this.gameControllers = gameControllers;
	}

	@Override
    public String toString() {
        return super.toString() + " " + Arrays.toString(gameControllers);
    }
}
