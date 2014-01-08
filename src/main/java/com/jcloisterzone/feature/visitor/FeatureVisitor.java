package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Feature;

public interface FeatureVisitor<T> {

    /** if false is returned visiting is stopped immediately */
    //TODO change to void and stop by throwing exception
    boolean visit(Feature feature);

    /** helper method for retrieving simple results */
    T getResult();
}
