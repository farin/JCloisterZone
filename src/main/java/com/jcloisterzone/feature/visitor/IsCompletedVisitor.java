package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;

public class IsCompletedVisitor implements FeatureVisitor {

	private boolean isCompleted = true;

	@Override
	public boolean visit(Feature feature) {
		Completable completable = (Completable) feature;
		if (! completable.isPieceCompleted()) {
			isCompleted = false;
			return false;
		}
		return true;
	}

	public boolean isCompleted() {
		return isCompleted;
	}


}
