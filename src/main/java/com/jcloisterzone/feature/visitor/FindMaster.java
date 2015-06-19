package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public class FindMaster implements FeatureVisitor<Feature> {

	private Feature master;

	@Override
	public VisitResult visit(Feature feature) {
		if (master == null || master.getId() > feature.getId()) {
			master = feature;
		}
		return VisitResult.CONTINUE;
	}

	@Deprecated //
	public Feature getMasterFeature() {
		return master;
	}

	@Override
	public Feature getResult() {
		return master;
	}

}
