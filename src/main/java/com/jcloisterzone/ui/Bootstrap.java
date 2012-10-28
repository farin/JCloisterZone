package com.jcloisterzone.ui;

import java.beans.IntrospectionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class Bootstrap  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Ini config = new Ini();
    //private URLClassLoader pluginClassLoader;

    public static boolean isMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    public void loadConfig() {
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
    }

    public static void addURLToSystemClassLoader(URL url) throws IntrospectionException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> classLoaderClass = URLClassLoader.class;

        try {
            java.lang.reflect.Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(systemClassLoader, new Object[] { url });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IntrospectionException("Error when adding url to system ClassLoader ");
        }
    }

    public URL fixPluginURL(URL url) throws MalformedURLException {
        //TODO do not add / for jar files
        return new URL(url.toString()+"/");
    }

    public void loadPlugins() {
        //List<URL> plugins = Lists.newArrayList();

        for (String pluginPath : config.get("plugins").getAll("plugin")) {
            try {
                URL pluginURL = Bootstrap.class.getClassLoader().getResource(pluginPath);
                pluginURL = fixPluginURL(pluginURL);
                //plugins.add(pluginURL);

                //temporaru solution
                addURLToSystemClassLoader(pluginURL);
                logger.info("plugin {} loaded", pluginURL);
                //devel
                //File plugin = new File(pluginURL.toURI());
                //System.out.println(plugin.getAbsoluteFile());
            } catch (Exception e) {
                logger.error("Unable to load plugin " + pluginPath, e);
            }
        }

//        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
//        pluginClassLoader = new URLClassLoader(plugins.toArray(new URL[plugins.size()]), currentThreadClassLoader);
//        Thread.currentThread().setContextClassLoader(pluginClassLoader);
    }

    public void run() {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCloisterZone");

        loadConfig();
        loadPlugins();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client(config);

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
