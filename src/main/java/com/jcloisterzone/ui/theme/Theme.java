package com.jcloisterzone.ui.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.legacy.FigureImageFilter;

public abstract class Theme {

    protected final transient Logger logger = LoggerFactory
            .getLogger(getClass());

    private final Client client;
    private final String baseDir;

    private Map<String, Image> imageCache = new WeakHashMap<String, Image>(64);

    public Theme(String baseDir, Client client) {
        this.baseDir = baseDir;
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    protected Map<String, Image> getImageCache() {
        return imageCache;
    }

    protected List<URL> getResourceLayers(String name) {
        int i = 0;
        List<URL> layers = new ArrayList<>();
        for (;;) {
            URL url = getResource(name + "_" + i + ".png", i > 0);
            if (url == null)
                break;
            layers.add(url);
            i++;
        }
        return layers;
    }

    protected URL getResource(String relativePath) {
        return getResource(relativePath, false);
    }

    private URL getResource(String relativePath, boolean silent) {
        // System.err.println(baseDir + "/" + relativePath);
        URL result = Theme.class.getClassLoader().getResource(
                baseDir + "/" + relativePath);
        if (result == null && !silent) {
            logger.error("Unable to load resource \"" + relativePath + "\"");
        }
        return result;
    }

    protected Image getImageResource(String relativePath) {
        return Toolkit.getDefaultToolkit().getImage(getResource(relativePath));
    }

    protected Image getLayeredImage(String name, Color color) {
        String key;
        if (color == null) {
            key = name;
        } else {
            key = name + '#' + color.getRGB();
        }
        Image image = imageCache.get(key);
        if (image == null) {
            List<URL> layers = getResourceLayers(name);
            image = composeImages(layers, color);
            imageCache.put(key, image);
        }
        return image;
    }

    protected Image getImage(String name) {
        Image image = imageCache.get(name);
        if (image == null) {
            image = (new ImageIcon(getImageResource(name))).getImage();
            imageCache.put(name, image);
        }
        return image;
    }

    protected Image composeImages(Iterable<URL> layers, Color color) {
        BufferedImage result = null;
        Graphics2D g = null;

        ImageFilter colorfilter = null;
        if (color != null) {
            colorfilter = new FigureImageFilter(color);
        }

        for (URL layer : layers) {
            // Image img = new ImageIcon(getResource(path)).getImage();
            Image img = Toolkit.getDefaultToolkit().createImage(layer);
            if (colorfilter != null) {
                img = Toolkit.getDefaultToolkit().createImage(
                        new FilteredImageSource(img.getSource(), colorfilter));
            }
            img = (new ImageIcon(img)).getImage(); // wait for load
            if (g == null) {
                result = UiUtils.newTransparentImage(img.getWidth(null),
                        img.getHeight(null));
                g = result.createGraphics();
            }
            // bez new ImgIcon nefunguje - vyzkoumat proc
            g.drawImage(img, 0, 0, null);
        }

        g.dispose();
        return result;
    }

}
