package com.jcloisterzone.ai;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
	private final Operation operation;
	private final Phase phase;
	private final Map<Expansion, ExpandedGame> frozenExpandedGames = Maps.newHashMap();

	public SavePoint(Operation operation, Game game) {
		this.operation = operation;
		this.phase = game.getPhase();
		for(Entry<Expansion, ExpandedGame> entry : game.getExpandedGamesMap().entrySet()) {
			ExpandedGame copy = entry.getValue().copy();
			if (copy != null) {
				frozenExpandedGames.put(entry.getKey(), copy);
			}
		}
	}

	public Operation getOperation() {
		return operation;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public Map<Expansion, ExpandedGame> getFrozenExpandedGames() {
		return frozenExpandedGames;
	}
}