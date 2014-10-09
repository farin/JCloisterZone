package com.jcloisterzone.event;

import com.jcloisterzone.game.Game;

public class GameListChangedEvent extends Event {

    private Game[] games;

    public GameListChangedEvent(Game[] games) {
        super();
        this.games = games;
    }

    public Game[] getGames() {
		return games;
	}

	public void setGames(Game[] games) {
		this.games = games;
	}

	@Override
    public String toString() {
        return super.toString() + " " + games;
    }
}
