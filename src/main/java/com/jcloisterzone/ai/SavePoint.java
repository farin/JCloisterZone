package com.jcloisterzone.ai;

import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
	private final Operation operation;
	private final Phase phase;

	public SavePoint(Operation operation, Phase phase) {
		this.operation = operation;
		this.phase = phase;
	}

	public Operation getOperation() {
		return operation;
	}

	public Phase getPhase() {
		return phase;
	}
}