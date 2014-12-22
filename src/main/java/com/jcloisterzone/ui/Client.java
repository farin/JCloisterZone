package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.transform.TransformerException;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.Player;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.AboutDialog;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.gtk.MenuFix;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ChannelPanel;
import com.jcloisterzone.ui.panel.ConnectGamePanel;
import com.jcloisterzone.ui.panel.ConnectPanel;
import com.jcloisterzone.ui.panel.ConnectPlayOnlinePanel;
import com.jcloisterzone.ui.plugin.Plugin;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.resources.PlugableResourceManager;
import com.jcloisterzone.ui.theme.ControlsTheme;
import com.jcloisterzone.ui.theme.FigureTheme;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.ui.view.UiView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.SimpleServer;

@SuppressWarnings("serial")
public class Client extends JFrame {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    public static final String BASE_TITLE = "JCloisterZone";

    private final Config config;
    private final ConfigLoader configLoader;
    private final ConvenientResourceManager resourceManager;

    @Deprecated
    private FigureTheme figureTheme;
    @Deprecated
    private ControlsTheme controlsTheme;

    private UiView view;
    private Activity activity;

    //@Deprecated
    //private GamePanel gamePanel;
    @Deprecated  //keep only view interface
    private ChannelPanel channelPanel;

    private ConnectPanel connectPanel;
    private DiscardedTilesDialog discardedTilesDialog;

    private final AtomicReference<SimpleServer> localServer = new AtomicReference<>();
    private Connection conn;

    @Deprecated
    private Game game;


    public Client(ConfigLoader configLoader, Config config, List<Plugin> plugins) {
        this.configLoader = configLoader;
        this.config = config;
        resourceManager = new ConvenientResourceManager(new PlugableResourceManager(this, plugins));
    }

    public void mountView(UiView view) {
    	if (this.view != null) {
    		this.view.hide();
    	}
    	cleanContentPane();
    	view.show(getContentPane());
    	getContentPane().setVisible(true);
    	this.view = view;
    	logger.info("{} mounted", view.getClass().getSimpleName());
    }

    public UiView getView() {
    	return view;
    }

    public void init() {
        setLocale(config.getLocaleObject());
        figureTheme = new FigureTheme(this);
        controlsTheme = new ControlsTheme(this);

        resetWindowIcon();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MenuFix.installGtkPopupBugWorkaround();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (closeGame()) {
                    System.exit(0);
                }
            }
        });
        MenuBar menuBar = new MenuBar(this);
        this.setJMenuBar(menuBar);

        //Toolkit.getDefaultToolkit().addAWTEventListener(new GlobalKeyListener(), AWTEvent.KEY_EVENT_MASK);

        mountView(new StartView(this));
        this.pack();

        String windowSize = config.getDebug() == null ? null : config.getDebug().getWindow_size();
        if (windowSize == null || "fullscreen".equals(windowSize)) {
        	this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        } else {
        	String[] sizes = windowSize.split("x");
        	if (sizes.length == 2) {
        		UiUtils.centerDialog(this, Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]));
        	} else {
        		logger.warn("Invalid configuration value for windows_size");
        		this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        	}
        }
        this.setTitle(BASE_TITLE);
        this.setVisible(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ev) {
				if (!Client.this.isActive()) return false; //AWT method on window (it not check if player is active)
				if (view == null) return false;
				return view.dispatchKeyEvent(ev);
			}
        });
    }

    @Override
    public MenuBar getJMenuBar() {
        return (MenuBar) super.getJMenuBar();
    }

    void resetWindowIcon() {
        this.setIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
    }

    public Config getConfig() {
        return config;
    }

    public void saveConfig() {
        configLoader.save(config);
    }

    public ConvenientResourceManager getResourceManager() {
        return resourceManager;
    }

    @Deprecated
    public FigureTheme getFigureTheme() {
        return figureTheme;
    }

    @Deprecated
    public ControlsTheme getControlsTheme() {
        return controlsTheme;
    }

    //should be referenced from Controller
    public Connection getConnection() {
        return conn;
    }

    @Deprecated  //TODO game per GamePanel
    public Game getGame() {
        return game;
    }


    public ConnectPanel getConnectGamePanel() {
        return connectPanel;
    }

    public void setDiscardedTilesDialog(DiscardedTilesDialog discardedTilesDialog) {
        this.discardedTilesDialog = discardedTilesDialog;
    }

    public void cleanContentPane() {
        Container pane = getContentPane();
        pane.setVisible(false);
        pane.removeAll();
        this.connectPanel = null;
    }



    public ChannelPanel newChannelPanel(ChannelController cc, String name) {
        Container pane = this.getContentPane();
        cleanContentPane();
        channelPanel = new ChannelPanel(this, cc);
        pane.add(channelPanel);
        pane.setVisible(true);
        //activity = ...
        return channelPanel;
    }

    public boolean closeGame() {
        return closeGame(false);
    }

    public boolean closeGame(boolean force) {
        boolean isGameRunning = getJMenuBar().isGameRunning();
        if (config.getConfirm().getGame_close() && isGameRunning && !(game.getPhase() instanceof GameOverPhase)) {
            if (localServer != null) {
                String options[] = {_("Close game"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(this,
                        _("Game is running. Do you really want to quit game and also disconnect all other players?"),
                        _("Close game"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.OK_OPTION != result) return false;
            } else {
                String options[] = {_("Close game"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(this,
                        _("Game is running. Do you really want to leave it?"),
                        _("Close game"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.OK_OPTION != result) return false;
            }
        }

        setTitle(BASE_TITLE);
        resetWindowIcon();
        if (conn != null) {
            conn.close();
            conn = null;
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
        getJMenuBar().setIsGameRunning(false);

        //TODO decouple
        if (view instanceof GameView) {
        	MainPanel mainPanel = ((GameView)view).getMainPanel();
        	if (mainPanel.getControlPanel() != null) mainPanel.getControlPanel().closeGame();
        	mainPanel.closeGame();
        }

        if (discardedTilesDialog != null) {
            discardedTilesDialog.dispose();
            discardedTilesDialog = null;
            getJMenuBar().setShowDiscardedEnabled(false);
        }
        return true;
    }

    public void showConnectGamePanel() {
        if (!closeGame()) return;

        Container pane = this.getContentPane();
        cleanContentPane();

        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel
        ConnectGamePanel panel = new ConnectGamePanel(this);
        envelope.add(panel);
        connectPanel = panel;

        pane.add(envelope, BorderLayout.CENTER);
        pane.setVisible(true);
    }

    public void showConnectPlayOnlinePanel() {
        if (!closeGame()) return;

        Container pane = this.getContentPane();
        cleanContentPane();

        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel
        ConnectPlayOnlinePanel panel = new ConnectPlayOnlinePanel(this);
        envelope.add(panel);
        connectPanel = panel;

        pane.add(envelope, BorderLayout.CENTER);
        pane.setVisible(true);
    }

    @Deprecated
    public void setGame(Game game) {
        this.game = game;
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
        ClientMessageListener handler = new ClientMessageListener(this);
        try {
            URI uri = new URI("ws", null, "".equals(hostname) ? "localhost" : hostname, port, playOnline ? "/ws" : "/", null, null);
            conn = handler.connect(username == null ? getUserName() : username, uri);
            conn.setReportingTool(reportingTool = new ReportingTool());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void handleSave() {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir") + System.getProperty("file.separator") + "saves");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle(_("Save game"));
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileFilter(new SavegameFileFilter());
        fc.setLocale(getLocale());
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                if (!file.getName().endsWith(".jcz")) {
                    file = new File(file.getAbsolutePath() + ".jcz");
                }
                try {
                    Snapshot snapshot = new Snapshot(game);
                    DebugConfig debugConfig = getConfig().getDebug();
                    if (debugConfig != null && "plain".equals(debugConfig.getSave_format())) {
                        snapshot.setGzipOutput(false);
                    }
                    snapshot.save(new FileOutputStream(file));
                } catch (IOException | TransformerException ex) {
                    logger.error(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void createGame() {
        createGame(null);
    }

    public void createGame(Snapshot snapshot) {
        if (closeGame()) {
            int port = config.getPort() == null ? ConfigLoader.DEFAULT_PORT : config.getPort();
            SimpleServer server = new SimpleServer(new InetSocketAddress(port), this);
            localServer.set(server);
            server.createGame(snapshot);
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

    public void handleLoad() {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir") + System.getProperty("file.separator") + "saves");
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
                    createGame(new Snapshot(file));
                } catch (IOException | SAXException ex1) {
                    //do not create error.log
                    JOptionPane.showMessageDialog(this, ex1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void handleQuit() {
        if (closeGame() == true) {
            System.exit(0);
        }
    }

    public void handleAbout() {
        new AboutDialog();
    }


    void beep() {
        if (config.getBeep_alert()) {
            try {
                playResourceSound("beep.wav");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
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
    private void playResourceSound(String resourceFilename) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
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

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    //TODO pass to view
    public void onWebsocketError(Exception ex) {
    	ConnectPanel cgp = getConnectGamePanel();
        if (cgp != null) {
            cgp.onWebsocketError(ex);
            return;
        }
        GridPanel gp = getGridPanel();
        String msg = ex.getMessage();
        if (ex instanceof WebsocketNotConnectedException) {
            if (game.isStarted()) {
                msg = _("Connection lost") + " - save game and load on server side and then connect with client as workaround" ;
            } else {
                msg = _("Connection lost");
            }
        } else {
            logger.error(ex.getMessage(), ex);
        }
        if (msg == null || msg.length() == 0) {
            msg = ex.getClass().getSimpleName();
        }
        if (gp != null) {
            gp.setErrorMessage(msg);
        } else {
            JOptionPane.showMessageDialog(this, msg, _("Error"), JOptionPane.ERROR_MESSAGE);
        }
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

    @Deprecated
    public Color getPlayerSecondTunelColor(Player player) {
        //TODO more effective implementation, move it to tunnel capability
        int slotNumber = player.getSlot().getNumber();
        PlayerSlot fakeSlot = new PlayerSlot((slotNumber + 2) % PlayerSlot.COUNT);
        return getConfig().getPlayerColor(fakeSlot).getMeepleColor();
    }
}