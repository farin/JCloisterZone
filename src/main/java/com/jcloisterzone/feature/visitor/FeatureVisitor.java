package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public interface FeatureVisitor<T> {

	/** if false is returned visiting is stopped immediately */
	boolean visit(Feature feature);
	
	/** helper method for retrieving simple results */
	T getResult();
}
