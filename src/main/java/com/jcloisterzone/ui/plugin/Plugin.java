package com.jcloisterzone.ui.plugin;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XMLUtils;

public abstract class Plugin {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final URL url;
    private final URLClassLoader loader;
    private final String relativePath;

    private String id;
    private String title;
    private String description;
    private PluginType type;

    private boolean loaded;
    private boolean enabled;

    public Plugin(URL url, String relativePath) throws MalformedURLException {
        this.relativePath = relativePath;
        this.url = fixPluginURL(url);
        logger.debug("Creating plugin loader for URL {}", this.url);
        loader = new URLClassLoader(new URL[] { this.url });
    }

    private URL fixPluginURL(URL url) throws MalformedURLException {
        boolean isFile = url.toString().endsWith(".jar") || url.toString().endsWith(".zip");
        if (isFile) return url;
        return new URL(url.toString()+"/");
    }

    protected final void loadMetadata() throws Exception {
        Element plugin = XMLUtils.parseDocument(loader.getResource("plugin.xml")).getDocumentElement();
        parseMetadata(plugin);
    }

    protected void parseMetadata(Element rootElement) throws Exception {
        id = rootElement.getAttribute("id");
        NodeList nl = rootElement.getElementsByTagName("title");
        if (nl.getLength() > 0) {
            title = nl.item(0).getTextContent();
        }
        nl = rootElement.getElementsByTagName("description");
        if (nl.getLength() > 0) {
            description = nl.item(0).getTextContent();
        }
        nl = rootElement.getElementsByTagName("type");
        if (nl.getLength() > 0) {
            type = PluginType.valueOf(nl.item(0).getTextContent());
        }
    }

    public Image getImageResource(String path) {
        logger.debug("Trying to load image resource {}:{}", getTitle(), path);
        URL url = getLoader().getResource(path);
        if (url == null) return null;
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    public Image getIcon() {
        return getImageResource("icon.jpg");
    }

    public URL getUrl() {
        return url;
    }
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getRelativePath() {
        return relativePath;
    }


    public PluginType getType() {
        return type;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public URLClassLoader getLoader() {
        return loader;
    }

    @Override
    public String toString() {
        return title;
    }

    //TOOD better throws own Exception
    public final void load() throws Exception {
        if (!isLoaded()) {
           doLoad();
           loaded = true;
        }
    }

    protected abstract void doLoad() throws Exception;

    private static Plugin readPlugin(URL pluginURL, String relativePath) throws Exception {
        ResourcePlugin plugin = new ResourcePlugin(pluginURL, relativePath);
        plugin.loadMetadata();
        return plugin;
    }

    public static Plugin readPlugin(Path path) throws Exception {
        Path basePath = Paths.get(ClassLoader.getSystemClassLoader().getResource(".").toURI());
        String relativePath = path.toString().substring(basePath.toString().length()+1);
        if (File.separatorChar != '/') {
            relativePath = relativePath.replace(File.separatorChar, '/');
        }
        return readPlugin(path.toUri().toURL(), relativePath);
    }
}

