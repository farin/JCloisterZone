package com.jcloisterzone.ui;

import java.net.MalformedURLException;
import java.net.URL;
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
import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.FileTeeStream;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.ui.plugin.Plugin;

public class Bootstrap  {

    {
        //run before first logger is initialized
        if (!"false".equals(System.getProperty("errorLog"))) {
            System.setOut(new FileTeeStream(System.out, "error.log"));
        }
    }

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
        LinkedList<Plugin> plugins = new LinkedList<>();
        List<String> pluginPaths = null;

        if (config.get("plugins") != null) {
            pluginPaths = config.get("plugins").getAll("plugin");
        }

        if (pluginPaths != null) {
            for (String pluginPath : pluginPaths) {
                try {
                    Plugin plugin = Plugin.loadPlugin(pluginPath);
                    plugins.addFirst(plugin);
                    logger.info("plugin <{}> loaded", plugin);
                } catch (Exception e) {
                    logger.error("Unable to load plugin " + pluginPath, e);
                }
            }
        }

        return plugins;
    }

    private void checkForUpdate(Ini config, final Client client) {
        final String updateUrlStr = config.get("update", "url");
        if (updateUrlStr != null && !com.jcloisterzone.Application.VERSION.contains("dev")) {
            (new Thread() {
                public void run() {
                    try {
                        URL url = new URL(updateUrlStr);
                        final AppUpdate update = AppUpdate.fetch(url);
                        if (update != null && (new VersionComparator()).compare(com.jcloisterzone.Application.VERSION, update.getVersion()) < 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    client.showUpdateIsAvailable(update);
                                };
                            });
                        }
                    } catch (MalformedURLException e) {
                        logger.error("Malformed key update.url in config file.", e);
                    }
                };
            }).start();
        }
    }

    public void run() {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCloisterZone");

        Ini config = loadConfig();
        List<Plugin> plugins = loadPlugins(config);

        final Client client = new Client(config, plugins);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                client.init();

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

        checkForUpdate(config, client);
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
