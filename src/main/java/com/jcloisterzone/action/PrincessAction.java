package com.jcloisterzone.action;

import com.jcloisterzone.PlayerRestriction;


public class PrincessAction extends UndeployAction {

    public PrincessAction() {
        super("princess", PlayerRestriction.any());
    }

    @Override
    protected int getSortOrder() {
        return 1;
    }
}
