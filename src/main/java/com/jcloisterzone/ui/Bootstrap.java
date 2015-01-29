package com.jcloisterzone.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.FileTeeStream;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.ui.plugin.Plugin;

public class Bootstrap  {

    private Path getDataDirectory(String basePath, String dirName) {
        if (basePath == null || basePath.length() == 0) return null;
        Path path = Paths.get(basePath);
        if (!Files.isWritable(path)) return null;
        path = path.resolve(dirName);
        File dir = path.toFile();
        if (dir.exists()) return path;
        return dir.mkdir() ? path : null;
    }

    //dO not use logger in this method!
    private Path getDataDirectory() {
        //jar file directory (better then user.dir which can point to user home and is quite useless
        String jarPath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        if (jarPath.matches("/.:/.*")) {
            //remove leading / for Windows paths - otherways Paths.get fails
            jarPath = jarPath.substring(1);
        }
        Path workingDir = Paths.get(jarPath).normalize().toAbsolutePath();
        Path path = workingDir;
        if (Files.isWritable(path)) {
            return path;
        }
        path = getDataDirectory(System.getenv("APPDATA"), "JCloisterZone");
        if (path != null) return path;
        path = getDataDirectory(System.getProperty("user.home"), ".jcloisterzone");
        if (path != null) return path;
        System.err.println(System.getProperty("user.home"));
        System.err.println("Could not locate writeable working dir");
        //returns user's working directory anyway //but configuration saving will not work
        return workingDir;
    }

    private Path dataDirectory = getDataDirectory();

    {
        //run before first logger is initialized
        if (!"false".equals(System.getProperty("errorLog"))) {
            FileTeeStream teeStream = new FileTeeStream(System.out, dataDirectory.resolve("error.log"));
            System.setOut(teeStream);
            System.setErr(teeStream);
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    public List<Plugin> loadPlugins(Config config) {
        LinkedList<Plugin> plugins = new LinkedList<>();
        List<String> pluginPaths = config.getPlugins();

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

    private void checkForUpdate(Config config, final Client client) {
        final String updateUrlStr = config.getUpdate();
        if (updateUrlStr != null && !com.jcloisterzone.Application.VERSION.contains("dev")) {
            (new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(updateUrlStr);
                        final AppUpdate update = AppUpdate.fetch(url);
                        if (update != null && (new VersionComparator()).compare(com.jcloisterzone.Application.VERSION, update.getVersion()) < 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
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

        logger.info("Date directory {}", dataDirectory.toString());

        ConfigLoader configLoader = new ConfigLoader(dataDirectory);
        Config config = configLoader.load();

        I18nUtils.setLocale(config.getLocaleObject()); //must be set before Expansions enum is initialized

        List<Plugin> plugins = loadPlugins(config);

        final Client client = new Client(dataDirectory, configLoader, config, plugins);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                client.init();

                if (isMac()) {
                    Application macApplication = Application.getApplication();
                    macApplication.setDockIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
                    macApplication.addApplicationListener(new MacApplicationAdapter(client));
                }

                DebugConfig debugConfig = client.getConfig().getDebug();
                if (debugConfig != null && debugConfig.isAutostartEnabled()) {
                    if (Boolean.TRUE.equals(debugConfig.getAutostart().getOnline())) {
                        client.connectPlayOnline(null);
                    } else {
                        client.createGame();
                    }
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
