package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.action.MageAndWitchAction;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;

public class MageAndWitchActionWrapper extends ActionWrapper {

    public MageAndWitchActionWrapper(MageAndWitchAction action) {
        super(action);
    }

    @Override
    public MageAndWitchAction getAction() {
        return (MageAndWitchAction) super.getAction();
    }

    @Override
    protected Image getImage(ResourceManager rm, Color color) {
        String name = getAction().getFigureId().startsWith("mage") ? "mage": "witch";
        return rm.getLayeredImage(new LayeredImageDescriptor("actions/" + name, color));
    }

}
