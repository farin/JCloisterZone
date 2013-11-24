package com.jcloisterzone.ui.plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XmlUtils;

public class Plugin {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private URL url;
    private URLClassLoader loader;

    private String id;
    private String title;
    private String description;

    public Plugin(URL url) throws MalformedURLException {
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
        Element plugin = XmlUtils.parseDocument(loader.getResource("plugin.xml")).getDocumentElement();
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

    public URLClassLoader getLoader() {
        return loader;
    }

    @Override
    public String toString() {
        return title;
    }

    public static Plugin loadPlugin(String path) throws Exception {
         URL pluginURL = Plugin.class.getClassLoader().getResource(path);
         ResourcePlugin plugin = new ResourcePlugin(pluginURL);
         plugin.loadMetadata();
         return plugin;
    }
}

