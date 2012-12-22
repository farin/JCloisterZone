package com.jcloisterzone.ui.plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Plugin {

    private URL url;
    private URLClassLoader loader;

    private String id;
    private String title;
    private String description;

    public Plugin(URL url) throws MalformedURLException {
        this.url = fixPluginURL(url);
        loader = new URLClassLoader(new URL[] { this.url });
    }

    private URL fixPluginURL(URL url) throws MalformedURLException {
        //TODO do not add / for jar files
        return new URL(url.toString()+"/");
    }

    protected void loadMetadata() throws Exception {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Element plugin = docBuilder.parse(loader.getResource("plugin.xml").openStream()).getDocumentElement();
        id = plugin.getAttribute("id");
        NodeList nl = plugin.getElementsByTagName("title");
        if (nl.getLength() > 0) {
            title = nl.item(0).getTextContent();
        }
        nl = plugin.getElementsByTagName("description");
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

