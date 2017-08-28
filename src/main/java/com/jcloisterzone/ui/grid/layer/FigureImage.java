package com.jcloisterzone.ui.grid.layer;

import java.awt.Image;

import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.ui.ImmutablePoint;

public class FigureImage {

    Figure<?> fig;
    ImmutablePoint offset;
    Image img;
    double scaleX, scaleY;

    private Image scaledImage;
    private int scaledForSize = -1;

    public FigureImage(Figure<?> fig) {
        this.fig = fig;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    public Image getScaledInstance(int baseSize) {
        if (scaledForSize != baseSize) {
            int width = (int) (baseSize * scaleX);
            int height = (int) (baseSize * scaleX);
            scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            scaledForSize = baseSize;
        }
        return scaledImage;
    }

    public Figure<?> getFigure() {
        return fig;
    }

    public ImmutablePoint getOffset() {
        return offset;
    }

    public Image getImg() {
        return img;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public Image getScaledImage() {
        return scaledImage;
    }

    public int getScaledForSize() {
        return scaledForSize;
    }

}
