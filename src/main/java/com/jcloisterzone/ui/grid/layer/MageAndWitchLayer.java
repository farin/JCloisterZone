package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

@Deprecated //better use Meple layer and create subclass of Meeple to reperesent Dragon, Fairy, Witch and Mage
public class MageAndWitchLayer extends AbstractGridLayer {

    private FeaturePointer mage, witch;
    private Image mageImage, witchImage;
    private Image scaledMageImage, scaledWitchImage;

    public MageAndWitchLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        mageImage = getClient().getFigureTheme().getNeutralImage("mage");
        witchImage = getClient().getFigureTheme().getNeutralImage("witch");
    }

    @Override
    public void paint(Graphics2D g2) {
        int boxSize = (int) (getSquareSize() * MeepleLayer.FIGURE_SIZE_RATIO * 1.2);
        if (mage != null) {
            ImmutablePoint offset = getClient().getResourceManager().getMeeplePlacement(gc.getGame().getBoard().get(mage.getPosition()), SmallFollower.class, mage.getLocation());
            offset = offset.translate(12, 2);
            offset = offset.scale(getSquareSize(), boxSize);
            if (scaledMageImage == null) {
                scaledMageImage = new ImageIcon(mageImage.getScaledInstance(boxSize, boxSize, Image.SCALE_SMOOTH)).getImage();
            }
            g2.drawImage(scaledMageImage, getOffsetX(mage.getPosition()) + offset.getX(), getOffsetY(mage.getPosition()) + offset.getY(), gridPanel);
        }
        if (witch != null) {
            ImmutablePoint offset = getClient().getResourceManager().getMeeplePlacement(gc.getGame().getBoard().get(witch.getPosition()), SmallFollower.class, witch.getLocation());
            offset = offset.translate(12, 2);
            offset = offset.scale(getSquareSize(), boxSize);
            if (scaledWitchImage == null) {
                scaledWitchImage = new ImageIcon(witchImage.getScaledInstance(boxSize, boxSize, Image.SCALE_SMOOTH)).getImage();
            }
            g2.drawImage(scaledWitchImage, getOffsetX(witch.getPosition()) + offset.getX(), getOffsetY(witch.getPosition()) + offset.getY(), gridPanel);
        }
    }

    @Override
    public void zoomChanged(int squareSize) {
        scaledMageImage = null;
        scaledWitchImage = null;
        super.zoomChanged(squareSize);
    }

    public FeaturePointer getMage() {
        return mage;
    }

    public void setMage(FeaturePointer mage) {
        this.mage = mage;
    }

    public FeaturePointer getWitch() {
        return witch;
    }

    public void setWitch(FeaturePointer witch) {
        this.witch = witch;
    }
}
