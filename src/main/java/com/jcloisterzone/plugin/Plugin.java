package com.jcloisterzone.plugin;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ExpansionType;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.plugin.PluginMeta.ExpansionMeta;
import com.jcloisterzone.ui.resources.ImageLoader;

import io.vavr.collection.Vector;

public abstract class Plugin {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final URL url;
    private final ImageLoader imageLoader;
    private final URLClassLoader loader;
    /** used to identify plugin and to be able save it back to config file */
    private final String relativePath;

    private PluginMeta meta;

    private boolean loaded;
    private boolean enabled;

    private Vector<Expansion> newExpansions = Vector.empty();

    public Plugin(URL url, String relativePath) throws MalformedURLException {
        this.relativePath = relativePath;
        this.url = fixPluginURL(url);
        logger.debug("Creating plugin loader for URL {}", this.url);
        loader = new URLClassLoader(new URL[] { this.url });
        imageLoader = new ImageLoader(loader);
    }

    private URL fixPluginURL(URL url) throws MalformedURLException {
        boolean isFile = url.toString().endsWith(".jar") || url.toString().endsWith(".zip");
        if (isFile) return url;
        return new URL(url.toString()+"/");
    }

    protected void loadMetadata() throws Exception {
        Yaml yaml = new Yaml(new Constructor(PluginMeta.class));
        meta = (PluginMeta) yaml.load(loader.getResource("plugin.yaml").openStream());

        if (meta.getExpansions() != null) {
            for (ExpansionMeta expMeta : meta.getExpansions()) {
                ExpansionType type = ExpansionType.valueOf(expMeta.getType());
                java.util.List<Class<? extends Capability<?>>> capabilityClasses = new ArrayList<>();
                for (String name : expMeta.getCapabilities()) {
                    Class<? extends Capability<?>> cls = Capability.classForName(name);
                    if (cls != null) {
                        capabilityClasses.add(cls);
                    }
                }
                @SuppressWarnings("unchecked")
                Class<? extends Capability<?>>[] _capabilityClasses = capabilityClasses.toArray(new Class[capabilityClasses.size()]);
                Expansion exp = new Expansion(expMeta.getName(), expMeta.getCode(), expMeta.getLabel(), _capabilityClasses, type);
                newExpansions = newExpansions.append(exp);
            }
        }
    }

    public Image getIcon() {
        return imageLoader.getImage("icon");
    }

    public URL getUrl() {
        return url;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public PluginMeta getMetadata() {
        return meta;
    }

    public boolean isDefault() {
        return getRelativePath().matches("^plugins/classic\\b");
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

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    public String toString() {
        return meta.getTitle();
    }

    //TOOD better throws own Exception
    public final void load() throws Exception {
        if (!isLoaded()) {
           doLoad();
           loaded = true;
        }
    }

    public final void unload() {
        if (isLoaded()) {
           doUnload();
           loaded = false;
        }
    }

    protected void doLoad() throws Exception {
        for (Expansion exp : newExpansions) {
            Expansion.register(exp, this);
        }
    }

    protected void doUnload() {
        for (Expansion exp : newExpansions) {
            Expansion.unregister(exp);
        }
    }

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

