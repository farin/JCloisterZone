package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public class FindMaster implements FeatureVisitor {

	private Feature master;

	@Override
	public boolean visit(Feature feature) {
		if (master == null || master.getId() > feature.getId()) {
			master = feature;
		}
		return true;
	}

	public Feature getMasterFeature() {
		return master;
	}

}
