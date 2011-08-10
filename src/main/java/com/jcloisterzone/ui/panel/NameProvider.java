package com.jcloisterzone.ui.panel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ini4j.Ini;

public class NameProvider {

	private LinkedList<String> playerNames;
	private List<String> aiNames;
	private int aiNameIdx;

	public NameProvider(Ini config) {
		List<String> playerNames = config.get("players").getAll("name");
		if (playerNames != null) {
			this.playerNames = new LinkedList<String>(playerNames);
		}
		aiNames = config.get("players").getAll("ai_name");
		if (aiNames == null) {
			aiNames = Collections.singletonList("?");
		}
	}

	synchronized
	public String getPlayerName() {
		if (playerNames == null || playerNames.isEmpty()) return "";
		return playerNames.removeFirst();
	}

	synchronized
	public String getAiName() {
		if (aiNameIdx == aiNames.size()) {
			aiNameIdx = 0;
		}
		return aiNames.get(aiNameIdx++);
	}

}
