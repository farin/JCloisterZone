package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;

public class IsOccupiedOrCompleted extends IsOccupied {
	
	private boolean isCompleted = true;
	
	@Override
	public boolean visit(Feature feature) {		
		Completable completable = (Completable) feature;
		if (completable.isOpen()) {
			isCompleted = false;
		}		
		return super.visit(feature);
	}
	
	@Override
	public Boolean getResult() {		
		return isCompleted || super.getResult();
	}
}
