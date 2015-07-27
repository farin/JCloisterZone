package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public interface FeatureVisitor<T> {

	public enum VisitResult {
		CONTINUE,
		STOP;
	}

	VisitResult visit(Feature feature);

    /** helper method for retrieving simple results */
    T getResult();
}
