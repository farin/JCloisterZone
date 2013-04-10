package com.jcloisterzone.action;


public class PrincessAction extends UndeployAction {

    public PrincessAction() {
        super("princess");
    }

    @Override
    protected int getSortOrder() {
        return 1;
    }
}
