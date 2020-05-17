package com.jcloisterzone.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.java_websocket.WebSocketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.FileTeeStream;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.plugin.NotAPluginException;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.plugin.PluginLoadException;

public class JCloisterZone  {

    public static String VERSION = "dev-snapshot";
    public static String BUILD_DATE = "";

    public static final String PROTCOL_VERSION = "4.6.0";

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

    private boolean isPluginEnabled(Config config, Path relPath) {
            String pluginName = relPath.toString();
        for (String path : config.getPlugins().getEnabled_plugins()) {
            if (pluginName.equals(path)) return true;
            //dev helper, match also unpacked plugins
            if (!pluginName.endsWith(".jar")) {
                if ((pluginName+".jar").equals(path)) return true;
            }
        }
        return false;
    }

    private boolean isPluginArchive(Path path) {
        String s = path.toString();
        return s.endsWith(".jar") || s.endsWith(".zip");
    }

    public List<Plugin> loadPlugins(Config config) {
        ArrayList<Plugin> plugins = new ArrayList<>();

        for (String folderName : config.getPlugins().getLookup_folders()) {
            try {
                Path pluginFolder = Paths.get(folderName);
                if (!pluginFolder.isAbsolute()) {
                    pluginFolder = Paths.get(getClass().getClassLoader().getResource(folderName).toURI());
                }
                DirectoryStream<Path> stream = Files.newDirectoryStream(pluginFolder);

                for (Path fullPath: stream) {
                        Path relPath = pluginFolder.relativize(fullPath);
                    boolean isValid = !relPath.toString().startsWith(".") && (
                       Files.isDirectory(fullPath) || isPluginArchive(fullPath)
                    );

                    if (!isValid) {
                        continue;
                    }

                    try {
                        Plugin plugin = Plugin.readPlugin(config, relPath, fullPath);
                        if (isPluginEnabled(config, relPath)) {
                            plugin.load();
                            plugin.setEnabled(true);
                        }
                        plugins.add(plugin);
                    } catch (NotAPluginException e1) {
                        logger.info("{} is not recognized as plugin", fullPath);
                    } catch (PluginLoadException e2) {
                        logger.error(String.format("Unable to load plugin %s", fullPath), e2);
                    }
                }
            } catch (URISyntaxException | IOException e) {
                logger.error("Cannot read plugin directory", e);
            }
        }


        Collections.sort(plugins, new Comparator<Plugin>() {

            private int getPluginPriority(Plugin p) {
                if (p.isDefault()) return Integer.MAX_VALUE;
                if (p.isExpansionSupported(Expansion.BASIC)) {
                    return 1000 + p.getContainedExpansions().size();
                } else {
                    return 10 + p.getContainedExpansions().size();
                }
            }

            @Override
            public int compare(Plugin o1, Plugin o2) {
                int o1ord = getPluginPriority(o1);
                int o2ord = getPluginPriority(o2);
                return o1ord - o2ord;
            }
        });

        //log after sort
        for (Plugin plugin: plugins) {
            logger.info("plugin <{}> loaded, enabled: {}", plugin.getRelativePath(), plugin.isEnabled());
        }

        return plugins;
    }

    private void checkForUpdate(Config config, final Client client) {
        final String updateUrlStr = config.getUpdate();
        if (updateUrlStr != null && !JCloisterZone.VERSION.contains("dev")) {
            (new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(updateUrlStr);
                        final AppUpdate update = AppUpdate.fetch(url);
                        if (update != null && (new VersionComparator()).compare(JCloisterZone.VERSION, update.getVersion()) < 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    client.showUpdateIsAvailable(update);
                                }
                            });
                        }
                    } catch (MalformedURLException e) {
                        logger.error("Malformed key update.url in config file.", e);
                    }
                }
            }).start();
        }
    }

    public void run() {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCloisterZone");

        if ("true".equals(System.getProperty("wsdebug"))) {
            WebSocketImpl.DEBUG = true;
        }

        logger.info("Data directory {}", dataDirectory.toString());

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
//                    manifest.getMainAttributes().forEach((k, v) -> {
//                        System.err.println(k + " -> " + v);
//                    });
//                    System.err.println("-----------");
                    if ("JCloisterZone".equals(manifest.getMainAttributes().getValue("Implementation-Title"))) {
                        String version = manifest.getMainAttributes().getValue("Implementation-Version");
                        if (version != null) {
                            VERSION = version;
                        }
                        String buildDate = manifest.getMainAttributes().getValue("Release-Date");
                        if (buildDate != null) {
                            BUILD_DATE = buildDate;
                        }
                    }
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }

        ConfigLoader configLoader = new ConfigLoader(dataDirectory);
        Config config = configLoader.load();

        I18nUtils.setLocale(config.getLocaleObject()); //must be set before Expansions enum is initialized

        List<Plugin> plugins = loadPlugins(config);

        final Client client = new Client(dataDirectory, configLoader, config, plugins);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                client.init();

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
        (new JCloisterZone()).run();
    }

}
