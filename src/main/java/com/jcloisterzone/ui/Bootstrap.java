package com.jcloisterzone.ui;

import java.beans.IntrospectionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.google.common.collect.Lists;
import com.jcloisterzone.ui.plugin.Plugin;

public class Bootstrap  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    public Ini loadConfig() {
        Ini config = new Ini();
        String configFile = System.getProperty("config");
        if (configFile == null) {
            configFile = "config.ini";
        }
        try {
            config.load(Client.class.getClassLoader().getResource(configFile));
        } catch (Exception ex) {
            logger.error("Unable to read config.ini", ex);
            System.exit(1);
        }
        return config;
    }

    public List<Plugin> loadPlugins(Ini config) {
        LinkedList<Plugin> plugins = Lists.newLinkedList();

        for (String pluginPath : config.get("plugins").getAll("plugin")) {
            try {
                Plugin plugin = Plugin.loadPlugin(pluginPath);
                plugins.addFirst(plugin);
                logger.info("plugin <{}> loaded", plugin);
            } catch (Exception e) {
                logger.error("Unable to load plugin " + pluginPath, e);
            }
        }

        return plugins;
    }

    public void run() {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCloisterZone");

        final Ini config = loadConfig();
        final List<Plugin> plugins = loadPlugins(config);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client(config, plugins);

                if (isMac()) {
                    Application macApplication = Application.getApplication();
                    macApplication.setDockIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
                    macApplication.addApplicationListener(new MacApplicationAdapter(client));
                }

                if (client.getConfig().get("debug", "autostart", boolean.class)) {
                    client.createGame();
                }
            }
        });
    }

    public static void main(String[] args) {
        (new Bootstrap()).run();
    }

    static class MacApplicationAdapter extends ApplicationAdapter {
        private final Client client;

        public MacApplicationAdapter(Client client) {
            this.client = client;
        }

        @Override
        public void handleAbout(ApplicationEvent ev) {
            ev.setHandled(true);
            client.handleAbout();
        }

        @Override
        public void handleQuit(ApplicationEvent ev) {
            ev.setHandled(true);
            client.handleQuit();
        }
    }
}
