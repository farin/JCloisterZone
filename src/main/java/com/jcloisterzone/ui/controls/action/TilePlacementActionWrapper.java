package com.jcloisterzone.ui.controls.action;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;

public class TilePlacementActionWrapper extends ActionWrapper implements ForwardBackwardListener {

    private Rotation tileRotation = Rotation.R0;
    private ForwardBackwardListener forwardBackwardDelegate;


    public TilePlacementActionWrapper(TilePlacementAction action) {
        super(action);
    }

    @Override
    public TilePlacementAction getAction() {
        return (TilePlacementAction) super.getAction();
    }

    @Override
    public Image getImage(ResourceManager rm, Player player, boolean active) {
        TileImage tileImg = rm.getTileImage(getAction().getTile(), tileRotation);
        Insets ins = tileImg.getOffset();
        Image img =  tileImg.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bi = UiUtils.newTransparentImage(w+2, h+2);
        Graphics2D ig = bi.createGraphics();
        ig.setColor(Color.BLACK);
        ig.drawRect(ins.left, ins.top, w+1-ins.left-ins.right, h+1-ins.top-ins.bottom);
        ig.drawImage(img, AffineTransform.getTranslateInstance(1, 1), null);
        return bi;
    }

    @Override
    public void forward() {
        forwardBackwardDelegate.forward();
    }

    @Override
    public void backward() {
        forwardBackwardDelegate.backward();
    }

    public ForwardBackwardListener getForwardBackwardDelegate() {
        return forwardBackwardDelegate;
    }

    public void setForwardBackwardDelegate(ForwardBackwardListener forwardBackwardDelegate) {
        this.forwardBackwardDelegate = forwardBackwardDelegate;
    }

    public Rotation getTileRotation() {
        return tileRotation;
    }

    public void setTileRotation(Rotation tileRotation) {
        this.tileRotation = tileRotation;
    }
}
