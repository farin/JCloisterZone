package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.ui.UiUtils;
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
        LayeredImageDescriptor lid = new LayeredImageDescriptor("actions/" + name, color);
        Image result = rm.getLayeredImage(lid);
        if (getAction().isCityOfCarcassoneMove()) {
            result = composeCityOfCarcassonneAction(
                result,
                rm.getImage("decorations/move_from_cc")
            );
        } else if (getAction().getOptions().get().getLocation().isCityOfCarcassonneQuarter()) {
            result = composeCityOfCarcassonneAction(
                result,
                rm.getImage("decorations/move_to_cc")
            );
        }
        return result;
    }

    private Image composeCityOfCarcassonneAction(Image image, Image decoration) {
        BufferedImage img = UiUtils.newTransparentImage(200, 200);
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        g2.drawImage(image, 35, 0, 130, 130, null);
        int w = decoration.getWidth(null);
        int h = decoration.getHeight(null);
        int resizedH = 100;
        int resizedW = w * resizedH / h;
        g2.drawImage(decoration, (200 - resizedW) / 2, 200 - resizedH, resizedW, resizedH, null);
        return img;
    }


}
