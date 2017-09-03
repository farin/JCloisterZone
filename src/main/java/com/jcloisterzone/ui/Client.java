package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.save.SavedGameParser;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.AboutDialog;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.dialog.HelpDialog;
import com.jcloisterzone.ui.dialog.PreferencesDialog;
import com.jcloisterzone.ui.dialog.TileDistributionWindow;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.gtk.MenuFix;
import com.jcloisterzone.ui.plugin.Plugin;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.resources.PlugableResourceManager;
import com.jcloisterzone.ui.theme.Theme;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.ui.view.UiView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.server.SimpleServer;
import com.jcloisterzone.wsio.server.SimpleServer.SimpleServerErrorHandler;

@SuppressWarnings("serial")
public class Client extends JFrame {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String BASE_TITLE = "JCloisterZone";

    private final Path dataDirectory;
    private final Config config;
    private final ConfigLoader configLoader;
    private final ConvenientResourceManager resourceManager;
    private final List<Plugin> plugins;

    private UiView view;
    private Theme theme;

    //TODO move to GameView
    private DiscardedTilesDialog discardedTilesDialog;
    private HelpDialog helpDialog;
    private AboutDialog aboutDialog;
    private TileDistributionWindow tileDistributionWindow;

    private final AtomicReference<SimpleServer> localServer = new AtomicReference<>();
    private ClientMessageListener clientMessageListener;

    private static Client instance;

    public Client(Path dataDirectory, ConfigLoader configLoader, Config config, List<Plugin> plugins) {
        instance = this;
        this.dataDirectory = dataDirectory;
        this.configLoader = configLoader;
        this.config = config;
        this.plugins = plugins;
        resourceManager = new ConvenientResourceManager(new PlugableResourceManager(plugins));
    }

    public static Client getInstance() {
        return instance;
    }

    public boolean mountView(UiView view) {
        return mountView(view, null);
    }

    public boolean mountView(UiView view, Object ctx) {
        if (this.view != null) {
            if (this.view.requestHide(view, ctx)) {
                this.view.hide(view, ctx);
            } else {
                return false;
            }
        }
        cleanContentPane();
        view.show(getContentPane(), ctx);
        getContentPane().setVisible(true);
        this.view = view;
        logger.info("{} mounted", view.getClass().getSimpleName());
        return true;
    }

    public UiView getView() {
        return view;
    }

    private void initWindowSize() {
        String windowSize = config.getDebug() == null ? null : config.getDebug().getWindow_size();
        if (System.getProperty("windowSize") != null) {
            windowSize = System.getProperty("windowSize");
        }
        if (windowSize == null || "fullscreen".equals(windowSize)) {
            this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        } else if ("L".equals(windowSize) || "R".equals(windowSize)) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int dw = gd.getDisplayMode().getWidth();
            int dh = gd.getDisplayMode().getHeight();
            setSize(dw/2, dh-40);
            setLocation("L".equals(windowSize) ? 0 : dw/2, 0);
        } else {
            String[] sizes = windowSize.split("x");
            if (sizes.length == 2) {
                UiUtils.centerDialog(this, Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]));
            } else {
                logger.warn("Invalid configuration value for windows_size");
                this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            }
        }
    }

    public void init() {
        setLocale(config.getLocaleObject());

        if ("dark".equalsIgnoreCase(config.getTheme())) {
            theme = Theme.DARK;
        } else {
            theme = Theme.LIGHT;
        }

        config.setDarkTheme(theme.isDark());

        resetWindowIcon();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MenuFix.installGtkPopupBugWorkaround();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        theme.setUiMangerDefaults();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuit();
            }
        });
        MenuBar menuBar = new MenuBar(this);
        this.setJMenuBar(menuBar);

        //Toolkit.getDefaultToolkit().addAWTEventListener(new GlobalKeyListener(), AWTEvent.KEY_EVENT_MASK);

        mountView(new StartView(this));
        this.pack();
        initWindowSize();
        this.setTitle(BASE_TITLE);
        this.setVisible(true);

        if (JCloisterZone.isMac()) {
            enableFullScreenMode();
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ev) {
                if (!Client.this.isActive()) return false; //AWT method on window (it not check if player is active)
                if (view == null) return false;
                return view.dispatchKeyEvent(ev);
            }
        });
    }

    private void enableFullScreenMode() {
        String className = "com.apple.eawt.FullScreenUtilities";
        String methodName = "setWindowCanFullScreen";

        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, new Class<?>[] { Window.class, boolean.class });
            method.invoke(null, this, true);
        } catch (Throwable e) {
            logger.info("OSX full screen mode isn't supported.", e);
        }
    }

    @Override
    public MenuBar getJMenuBar() {
        return (MenuBar) super.getJMenuBar();
    }

    void resetWindowIcon() {
        this.setIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
    }

    public Theme getTheme() {
        return theme;
    }

    public Config getConfig() {
        return config;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void saveConfig() {
        configLoader.save(config);
        resourceManager.clearCache();
    }

    public ConvenientResourceManager getResourceManager() {
        return resourceManager;
    }

    public SimpleServer getLocalServer() {
        return localServer.get();
    }

    //TODO should be referenced from Controller
    public Connection getConnection() {
        return clientMessageListener == null ? null : clientMessageListener.getConnection();
    }

    public ClientMessageListener getClientMessageListener() {
        return clientMessageListener;
    }

    public void setDiscardedTilesDialog(DiscardedTilesDialog discardedTilesDialog) {
        this.discardedTilesDialog = discardedTilesDialog;
    }

    public void cleanContentPane() {
        Container pane = getContentPane();
        pane.setVisible(false);
        pane.removeAll();
    }

    public boolean closeGame() {
        return closeGame(false);
    }

    public boolean closeGame(boolean force) {
        boolean isGameRunning = (view instanceof GameView) && ((GameView)view).isGameRunning();
        if (isGameRunning && !"false".equals(System.getProperty("closeGameConfirm"))) {
            if (localServer.get() != null) {
                String options[] = {_("Leave game"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(this,
                        _("The game is not finished. Do you really want to stop game and disconnect all other players?"),
                        _("Leave game"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.OK_OPTION != result) return false;
            } else {
                String options[] = {_("Leave game"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(this,
                        _("The game is not finished. Do you really want to leave it?"),
                        _("Leave game"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.OK_OPTION != result) return false;
            }
        }

        setTitle(BASE_TITLE);
        resetWindowIcon();
        if (clientMessageListener != null && !clientMessageListener.isPlayOnline()) {
            clientMessageListener.getConnection().close();
            clientMessageListener = null;
        }
        SimpleServer server = localServer.get();
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            localServer.set(null);
        }

        //TODO decouple
        if (view instanceof GameView) {
            ((GameView)view).closeGame();
        }

        if (discardedTilesDialog != null) {
            discardedTilesDialog.dispose();
            discardedTilesDialog = null;
        }
        return true;
    }

    private String getUserName() {
        if (System.getProperty("nick") != null) {
            return System.getProperty("nick");
        }
        String name = config.getClient_name();
        name = name == null ? "" : name.trim();
        if (name.equals("")) name = System.getProperty("user.name");
        if (name.equals("")) name = UUID.randomUUID().toString().substring(2, 6);
        return name;
    }

    public void connect(String hostname, int port) {
        connect(null, hostname, port, false);
    }

    public void connectPlayOnline(String username) {
        String configValue =  getConfig().getPlay_online_host();
        String[] hp = ((configValue == null || configValue.trim().length() == 0) ? ConfigLoader.DEFAULT_PLAY_ONLINE_HOST : configValue).split(":");
        int port = 80;
        if (hp.length > 1) {
           port = Integer.parseInt(hp[1]);
        }
        connect(username, hp[0], port, true);
    }


    private void connect(String username, String hostname, int port, boolean playOnline) {
        clientMessageListener = new ClientMessageListener(this, playOnline);
        try {
            URI uri = new URI("ws", null, "".equals(hostname) ? "localhost" : hostname, port, playOnline ? "/ws" : "/", null, null);
            logger.info("Connection to {}", uri);
            WebSocketConnection conn = clientMessageListener.connect(username == null ? getUserName() : username, uri);
            conn.setReportingTool(new ReportingTool());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void createGame() {
        createGame(null, null);
    }

    public void createGame(Game game) {
        createGame(null, game);
    }

    public void createGame(SavedGame savedGame) {
        createGame(savedGame, null);
    }

    private void createGame(SavedGame savedGame, Game game) {
        if (closeGame()) {
            int port = config.getPort() == null ? ConfigLoader.DEFAULT_PORT : config.getPort();
            SimpleServer server = new SimpleServer(new InetSocketAddress(port), new SimpleServerErrorHandler() {
                @Override
                public void onError(WebSocket ws, Exception ex) {
                    if (ex instanceof ClosedByInterruptException) {
                        logger.info(ex.toString()); //exception message is null
                    } else if (ex instanceof BindException) {
                        onServerStartError(ex);
                    } else {
                        logger.error(ex.getMessage(), ex);
                    }

                }
            });
            localServer.set(server);
            server.createGame(savedGame, game, config.getClient_id());
            server.start();
            try {
                //HACK - there is not success handler in WebSocket server
                //we must wait for start to now connect to
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //empty
            }
            if (localServer.get() != null) { //can be set to null by server error
                connect(null, "localhost", port, false);
            }
        }
    }

    //this method is not called from swing thread
    public void onServerStartError(final Exception ex) {
        localServer.set(null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(Client.this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    public File getSavesDirectory() {
        String savesFolderValue = getConfig().getSaved_games().getFolder();
        File savesFolder;
        if (savesFolderValue == null || savesFolderValue.isEmpty()) {
            savesFolder = dataDirectory.resolve("saves").toFile();
        } else {
            savesFolder = new File(savesFolderValue);
        }
        if (!savesFolder.exists()) {
            savesFolder.mkdir();
        }
        return savesFolder;
    }

    public File getScreenshotDirectory() {
        String screenFolderValue = getConfig().getScreenshots().getFolder();
        File folder;
        if (screenFolderValue == null || screenFolderValue.isEmpty()) {
            folder = dataDirectory.resolve("screenshots").toFile();
        } else {
            folder = new File(screenFolderValue);
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public void handleLoad() {
        JFileChooser fc = new JFileChooser(getSavesDirectory());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle(_("Load game"));
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileFilter(new SavegameFileFilter());
        fc.setLocale(getLocale());
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                try {
                    SavedGameParser parser = new SavedGameParser(getConfig());
                    JsonReader reader = new JsonReader(new FileReader(file));
                    SavedGame sg = parser.fromJson(reader);
                    createGame(sg);
                } catch (IOException ex) {
                    //do not create error.log
                    JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void handleQuit() {
        if (getView().requestHide(null, null)) {
            System.exit(0);
        }
    }

    public void showHelpDialog() {
        if (helpDialog == null) {
            helpDialog = new HelpDialog();
            helpDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    helpDialog = null;
                }
            });
        } else {
            helpDialog.toFront();
        }
    }

    public void showAboutDialog() {
        if (aboutDialog == null) {
            aboutDialog = new AboutDialog(this, config.getOrigin());
            aboutDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    aboutDialog = null;
                }
            });
        } else {
            aboutDialog.toFront();
        }
    }

    public void showPreferncesDialog() {
        new PreferencesDialog(this);
    }

    public void showTileDistribution() {
        if (tileDistributionWindow == null) {
            tileDistributionWindow = new TileDistributionWindow(this);
            tileDistributionWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    tileDistributionWindow = null;
                }
            });
        } else {
            tileDistributionWindow.toFront();
        }
    }


    void beep() {
        if (config.getBeep_alert()) {
            playSound("audio/beep.wav");
        }
    }

    /*
     * Map of resource filenames to sound clip objects. TODO: clean up clip
     * objects on destroy?
     */
    private final Map<String, Clip> resourceSounds = new HashMap<String, Clip>();

    /*
     * Load and play sound clip from resources by filename.
     */
    private void playResourceSound(String resourceFilename) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        // Load sound if necessary.
        if (!resourceSounds.containsKey(resourceFilename)) {
            BufferedInputStream resourceStream = loadResourceAsStream(resourceFilename);
            Clip loadedClip = loadSoundFromStream(resourceStream);
            resourceSounds.put(resourceFilename, loadedClip);
        }

        Clip clip = resourceSounds.get(resourceFilename);

        // Stop before starting, in case it plays rapidly (haven't tested).
        clip.stop();

        // Always start from the beginning
        clip.setFramePosition(0);
        clip.start();
    }

    public void playSound(String resourceFilename) {
        try {
            playResourceSound(resourceFilename);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private BufferedInputStream loadResourceAsStream(String filename)
            throws IOException {
        BufferedInputStream resourceStream = new BufferedInputStream(
                Client.class.getClassLoader().getResource(filename)
                        .openStream());

        return resourceStream;
    }

    /*
     * Pre-load sound clip so it can play from memory.
     */
    private Clip loadSoundFromStream(BufferedInputStream inputStream)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        AudioInputStream audioInputStream = AudioSystem
                .getAudioInputStream(inputStream);

        // Auto-detect file format.
        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);

        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(audioInputStream);

        // Don't need the stream anymore.
        audioInputStream.close();

        return clip;
    }


    public DiscardedTilesDialog getDiscardedTilesDialog() {
        return discardedTilesDialog;
    }

    public void showUpdateIsAvailable(final AppUpdate appUpdate) {
        if (isVisible() && view instanceof StartView) {
            ((StartView)view).showUpdateIsAvailable(appUpdate);
        } else {
            //probably it shouln't happen
            System.out.println("JCloisterZone " + appUpdate.getVersion() + " is avaiable for download.");
            System.out.println(appUpdate.getDescription());
            System.out.println(appUpdate.getDownloadUrl());
        }
    }

    public void onWebsocketError(Exception ex) {
        view.onWebsocketError(ex);
    }

    public void onWebsocketClose(int code, String reason, boolean remote) {
        view.onWebsocketClose(code, reason, remote);
    }

    public void onUnhandledWebsocketError(Exception ex) {
        String message;
        if (ex instanceof WebsocketNotConnectedException) {
            message = _("Connection lost");
        } else {
            message = ex.getMessage();
            if (message == null || message.length() == 0) {
                message = ex.getClass().getSimpleName();
            }
            logger.error(message, ex);
        }
        JOptionPane.showMessageDialog(this, message, _("Error"), JOptionPane.ERROR_MESSAGE);
    }

    //------------------- LEGACY: TODO refactor ---------------


    @Deprecated
    public ControlPanel getControlPanel() {
        MainPanel mainPanel = getMainPanel();
        if (mainPanel != null) return mainPanel.getControlPanel();
        return null;
    }

    @Deprecated
    public GridPanel getGridPanel() {
        MainPanel mainPanel = getMainPanel();
        if (mainPanel != null) return mainPanel.getGridPanel();
        return null;
    }

    @Deprecated
    public MainPanel getMainPanel() {
        if (view instanceof GameView) {
            return ((GameView)view).getMainPanel();
        }
        return null;
    }
}