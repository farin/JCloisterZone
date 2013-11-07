package com.jcloisterzone.feature.visitor;

public abstract class SelfReturningVisitor implements FeatureVisitor<SelfReturningVisitor> {
	
	@Override
	public SelfReturningVisitor getResult() {		
		return this;
	}

}
