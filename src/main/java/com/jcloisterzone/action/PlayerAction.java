package com.jcloisterzone.action;

public abstract class PlayerAction implements Comparable<PlayerAction> {

	public String getName() {
		return getClass().getSimpleName().toLowerCase().replace("action", "");
	}

	protected int getSortOrder() {
		return 1024;
	}

	@Override
	public int compareTo(PlayerAction o) {
		return getSortOrder() - o.getSortOrder();
	}

}
