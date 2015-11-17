package com.jcloisterzone.ui.resources;

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

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.legacy.FigureImageFilter;

public class ImageLoader {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final ClassLoader classLoader;


	private static final String[] EXTENSIONS = {".jpg", ".png"};

	public ImageLoader(ClassLoader classLoader) {
		this(classLoader,  "");
	}

	public ImageLoader(ClassLoader classLoader, String baseDir) {
		this.classLoader = classLoader;
	}

	private URL getResource(String relativePath) {
		//logger.debug("Trying to load resource {}", relativePath);
        return classLoader.getResource(relativePath);
    }

	public Image getImageResource(String path) {
        URL url = getResource(path);
        return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
    }

	/**
	 * @param full path without file extension
	 */
	public Image getImage(String baseName) {
		for (String ext : EXTENSIONS) {
			Image img = getImageResource(baseName + ext);
			if (img != null) {
				return (new ImageIcon(img)).getImage();
			}
		}
		return null;
    }

	public Image getLayeredImage(LayeredImageDescriptor lid) {
		List<URL> layers = getResourceLayers(lid.getBaseName());
		if (layers == null) return null;
		if (lid.getAdditionalLayer() != null) {
			URL url = getResource(lid.getAdditionalLayer() + ".png");
			if (url != null) {
				layers.add(url);
			}
		}
		return composeImages(layers, lid.getColorOverlay());
	}


	protected List<URL> getResourceLayers(String name) {
        int i = 0;
        List<URL> layers = null;
        for (;;) {
            URL url = getResource(name + "_" + i + ".png");
            if (url == null) break;
            if (layers == null) layers = new ArrayList<>(4);
            layers.add(url);
            i++;
        }
        return layers;
    }

	protected Image composeImages(Iterable<URL> layers, Color colorOverlay) {
        BufferedImage result = null;
        Graphics2D g = null;

        ImageFilter colorfilter = null;
        if (colorOverlay != null) {
            colorfilter = new FigureImageFilter(colorOverlay);
        }

        for (URL layer : layers) {
            Image img = Toolkit.getDefaultToolkit().createImage(layer);
            if (colorfilter != null) {
                img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), colorfilter));
            }
            img = (new ImageIcon(img)).getImage(); // wait for load
            if (g == null) {
                result = UiUtils.newTransparentImage(img.getWidth(null), img.getHeight(null));
                g = result.createGraphics();
            }
            // bez new ImgIcon nefunguje - vyzkoumat proc
            g.drawImage(img, 0, 0, null);
        }

        g.dispose();
        return result;
    }

}
