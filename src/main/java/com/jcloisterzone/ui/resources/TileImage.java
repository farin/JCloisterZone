package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.Insets;

public class TileImage {

    private final Image image;
    private final Insets offset;

    public TileImage(Image image, Insets offset) {
        this.image = image;
        this.offset = offset;
    }

    public Image getImage() {
        return image;
    }

    public Insets getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        return image.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TileImage)) return false;
        TileImage other = (TileImage) obj;
        return image.equals(other.image) && offset.equals(other.offset);
    }

}
