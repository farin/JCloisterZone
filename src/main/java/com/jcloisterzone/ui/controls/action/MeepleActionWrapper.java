package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;

public class MeepleActionWrapper extends ActionWrapper {

    public MeepleActionWrapper(MeepleAction action) {
        super(action);
    }

    @Override
    public MeepleAction getAction() {
        return (MeepleAction) super.getAction();
    }

    @Override
    protected Image getImage(ResourceManager rm, Color color) {
        String name = getAction().getMeepleType().getSimpleName().toLowerCase();
        return rm.getLayeredImage(new LayeredImageDescriptor("actions/" + name, color));
    }


}
