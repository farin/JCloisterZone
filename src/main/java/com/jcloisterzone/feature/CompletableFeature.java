package com.jcloisterzone.feature;



public abstract class CompletableFeature extends MultiTileFeature implements Completable {

	@Override
	public boolean isOpen() {
		for (MultiTileFeature edge : getEdges()) {
			if (edge == null) return true;
		}
		return false;
	}

}
