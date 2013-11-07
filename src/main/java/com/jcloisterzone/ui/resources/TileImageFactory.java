package com.jcloisterzone.ui.resources;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.ImageIcon;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.ui.plugin.Plugin;

public class TileImageFactory {

    private URLClassLoader loader;

    private static final int SIZE = 300;

    private Location[] BASE_ROTATIONS = new Location[] {
            Location.N,
            Location.NW,
            Location.WE,
            Location._N,
            Location.NWSE,
            Location.CLOISTER,
            Location.TOWER
    };

    public TileImageFactory() {
        //debug - TODO make plugin from this
        try {
            URL url = Plugin.class.getClassLoader().getResource("plugins/fallback");
            url = new URL(url.toString()+"/");
            loader = new URLClassLoader(new URL[] { url });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public URLClassLoader getLoader() {
        return loader;
    }

    protected Image getImageResource(String path) {
        Image img =  Toolkit.getDefaultToolkit().getImage(getLoader().getResource(path));
        return (new ImageIcon(img)).getImage();
    }

    //------------

    public Image getTileImage(Tile tile) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.drawImage(getImageResource("tiles/bg.png"), 0, 0, null);

        for (Feature f : tile.getFeatures()) {
            if (f instanceof Farm) continue;
            drawFeature(g2, f);
        }
        return img;
    }

    private void drawFeature(Graphics2D g2, Feature feature) {
        Location loc = feature.getRawLocation();
        //System.out.println(feature + " / " + loc);
        for (Location base : BASE_ROTATIONS) {
            Rotation rot = loc.getRotationOf(base);
            if (rot != null) {
                String path = feature.getClass().getSimpleName().toLowerCase();
                Image img = getImageResource("tiles/"+path+"/"+base.toString()+".png");
                g2.drawImage(img, rot.getAffineTransform(SIZE), null);
                break;
            }
        }
    }

    public Image getAbbeyImage() {
        return null;
    }
}
