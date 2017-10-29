package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.action.NeutralFigureAction;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;

public class NeutralFigureActionWrapper extends ActionWrapper {

    public NeutralFigureActionWrapper(NeutralFigureAction action) {
        super(action);
    }

    @Override
    public NeutralFigureAction getAction() {
        return (NeutralFigureAction) super.getAction();
    }

    @Override
    protected Image getImage(ResourceManager rm, Color color) {
        String name = getAction().getFigure().getClass().getSimpleName().toLowerCase();
        return rm.getLayeredImage(new LayeredImageDescriptor("actions/" + name, color));
    }

}
