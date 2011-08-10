package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public interface FeatureVisitor {

	/** if false is returned visiting is stopped immediately */
	boolean visit(Feature feature);
}
