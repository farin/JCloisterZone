package com.jcloisterzone.feature;

import com.jcloisterzone.feature.visitor.IsCompletedVisitor;


public abstract class CompletableFeature extends MultiTileFeature implements Completable {

	@Override
	public boolean isPieceCompleted() {
		for(MultiTileFeature edge : getEdges()) {
			if (edge == null) return false;
		}
		return true;
	}

	@Override
	public boolean isFeatureCompleted() {
		IsCompletedVisitor visitor = new IsCompletedVisitor();
		walk(visitor);
		return visitor.isCompleted();
	}


}
