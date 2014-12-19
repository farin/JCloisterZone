package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.Player;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.rmi.ClientStub;
import com.jcloisterzone.rmi.RmiProxy;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.AboutDialog;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.KeyController;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.gtk.MenuFix;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ConnectGamePanel;
import com.jcloisterzone.ui.panel.CreateGamePanel;
import com.jcloisterzone.ui.panel.GamePanel;
import com.jcloisterzone.ui.panel.HelpPanel;
import com.jcloisterzone.ui.panel.StartPanel;
import com.jcloisterzone.ui.plugin.Plugin;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.resources.PlugableResourceManager;
import com.jcloisterzone.ui.theme.ControlsTheme;
import com.jcloisterzone.ui.theme.FigureTheme;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.SimpleServer;

@SuppressWarnings("serial")
public class Client extends JFrame {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    public static final String BASE_TITLE = "JCloisterZone";

    private KeyController keyController;

    private final Config config;
    private final ConfigLoader configLoader;
    private final ConvenientResourceManager resourceManager;

    //non-persistent settings (TODO move to mainPanel)
    private boolean showHistory;

    @Deprecated
    private FigureTheme figureTheme;
    @Deprecated
    private ControlsTheme controlsTheme;
    //private PlayerColor[] playerColors;

    private GamePanel gamePanel;

    //private MenuBar menuBar;
    private StartPanel startPanel;

    private ConnectGamePanel connectGamePanel;
    private DiscardedTilesDialog discardedTilesDialog;

    private final AtomicReference<SimpleServer> localServer = new AtomicReference<>();
    private Connection conn;

    private Game game;
    //active player must be cached locally because of game's active player record is changed in other thread immediately
    private Player activePlayer;

    private EventBus eventBus;


    public Client(ConfigLoader configLoader, Config config, List<Plugin> plugins) {
        this.configLoader = configLoader;
        this.config = config;
        resourceManager = new ConvenientResourceManager(new PlugableResourceManager(this, plugins));
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
            e.printStackTrace(); //TODO logger
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (closeGame() == true) {
                    System.exit(0);
                }
            }
        });
        MenuBar menuBar = new MenuBar(this);
        this.setJMenuBar(menuBar);

        //Toolkit.getDefaultToolkit().addAWTEventListener(new GlobalKeyListener(), AWTEvent.KEY_EVENT_MASK);

        Container pane = getContentPane();

        pane.setLayout(new BorderLayout());
        JPanel envelope = new BackgroundPanel(new GridBagLayout());
        pane.add(envelope, BorderLayout.CENTER);

        startPanel = new StartPanel();
        startPanel.setClient(this);
        envelope.add(startPanel);

        this.pack();
        this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        this.setTitle(BASE_TITLE);
        this.setVisible(true);

        keyController = new KeyController(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyController);
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

    public Connection getConnection() {
        return conn;
    }

    public RmiProxy getServer() {
        return conn.getRmiProxy();
    }

    @Deprecated  //TODO game per GamePanel
    public Game getGame() {
        return game;
    }


    public ConnectGamePanel getConnectGamePanel() {
        return connectGamePanel;
    }

    public void setDiscardedTilesDialog(DiscardedTilesDialog discardedTilesDialog) {
        this.discardedTilesDialog = discardedTilesDialog;
    }

    public void cleanContentPane() {
        //this.requestFocus();
        Container pane = this.getContentPane();
        pane.setVisible(false);
        pane.removeAll();
        this.startPanel = null;
        if (gamePanel != null) {
            gamePanel.disposePanel();
            gamePanel = null;
        }
        this.connectGamePanel = null;
    }
    
   public void takeScreenshot()
   {
	   Container container = gamePanel.getGridPanel();
       BufferedImage im = new BufferedImage(container.getWidth(), container.getHeight(), BufferedImage.TYPE_INT_ARGB);
       JFileChooser fc = new JFileChooser(System.getProperty("user.dir") + System.getProperty("file.separator"));
       fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
       fc.setDialogTitle(_("Save Screenshot"));
       fc.setDialogType(JFileChooser.SAVE_DIALOG);
       fc.setFileFilter(new PNGFileFilter());
       fc.setLocale(getLocale());
       int returnVal = fc.showSaveDialog(this);
       if (returnVal == JFileChooser.APPROVE_OPTION) {
           File file = fc.getSelectedFile();
           if (file != null) {
               if (!file.getName().endsWith(".png")) {
                   file = new File(file.getAbsolutePath() + ".png");
               }
               try
               {
        	       FileOutputStream fos = new FileOutputStream(file);
        	       container.paint(im.getGraphics());
        	       ImageIO.write(im, "PNG", fos);
        	       fos.close();
               } catch (IOException ex) {
                   logger.error(ex.getMessage(), ex);
                   JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
               }
           }
       }
   }

    public void newGamePanel(Game game, boolean mutableSlots, PlayerSlot[] slots) {
        Container pane = this.getContentPane();
        cleanContentPane();
        gamePanel = new GamePanel(this, game);
        gamePanel.showCreateGamePanel(mutableSlots, slots);
        pane.add(gamePanel);
        pane.setVisible(true);
    }

    @Deprecated
    public CreateGamePanel getCreateGamePanel() {
        if (gamePanel == null) return null;
        return gamePanel.getCreateGamePanel();
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
        activePlayer = null;
        getJMenuBar().setIsGameRunning(false);

        if (gamePanel != null && gamePanel.getMainPanel() != null) {
            if (gamePanel.getControlPanel() != null) gamePanel.getControlPanel().closeGame();
            gamePanel.getMainPanel().closeGame();
        }
        if (discardedTilesDialog != null) {
            discardedTilesDialog.dispose();
            discardedTilesDialog = null;
            getJMenuBar().setShowDiscardedEnabled(false);
        }
        eventBus = null;
        return true;
    }

    public void showConnectGamePanel() {
        if (!closeGame()) return;

        Container pane = this.getContentPane();
        cleanContentPane();

        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel
        envelope.add(connectGamePanel = new ConnectGamePanel(this));

        pane.add(envelope, BorderLayout.CENTER);
        pane.setVisible(true);
    }

    public void setGame(Game game) {
        assert gamePanel != null;
        this.game = game;
        this.eventBus = new EventBus(new EventBusExceptionHandler("ui event bus"));
        game.setReportingTool(reportingTool);
        reportingTool.setGame(game);
        eventBus.register(new ClientController(this, game, gamePanel));
        InvokeInSwingUiAdapter uiAdapter = new InvokeInSwingUiAdapter(eventBus);
        uiAdapter.setReportingTool(reportingTool);
        game.getEventBus().register(uiAdapter);
    }

    private String getUserName() {
        String name = config.getClient_name();
        name = name == null ? "" : name.trim();
        if (name.equals("")) name = System.getProperty("user.name");
        if (name.equals("")) name = UUID.randomUUID().toString().substring(2, 6);
        return name;
    }

    public void connect(String hostname, int port) {
        ClientStub handler = new ClientStub(this);
        RmiProxy rmiProxy = (RmiProxy) Proxy.newProxyInstance(RmiProxy.class.getClassLoader(), new Class[] { RmiProxy.class }, handler);
        try {
            conn = handler.connect(getUserName(), hostname, port);
            conn.setReportingTool(reportingTool = new ReportingTool());
            conn.setRmiProxy(rmiProxy);
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
            SimpleServer server = new SimpleServer(new InetSocketAddress(config.getPort()), this);
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
                connect("localhost", config.getPort());
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

    void clearActions() {
        ControlPanel controlPanel = gamePanel.getControlPanel();
        ActionPanel ap = controlPanel.getActionPanel();
        if (ap.getActions() != null) {
            controlPanel.clearActions();
        }
        ap.setFakeAction(null);
        getJMenuBar().getUndo().setEnabled(false);
    }

    public DiscardedTilesDialog getDiscardedTilesDialog() {
        return discardedTilesDialog;
    }

    public void showUpdateIsAvailable(final AppUpdate appUpdate) {
        if (isVisible() && startPanel != null) {
            Color bg = new Color(0.2f, 1.0f, 0.0f, 0.1f);
            HelpPanel hp = startPanel.getHelpPanel();
            hp.removeAll();
            hp.setOpaque(true);
            hp.setBackground(bg);
            Font font = new Font(null, Font.BOLD, 14);
            JLabel label;
            label = new JLabel(_("JCloisterZone " + appUpdate.getVersion() + " is available for download."));
            label.setFont(font);
            hp.add(label, "wrap");
            label = new JLabel(appUpdate.getDescription());
            hp.add(label, "wrap");

            final JTextField link = new JTextField(appUpdate.getDownloadUrl());
            link.setEditable(false);
            link.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    link.setSelectionStart(0);
                    link.setSelectionEnd(link.getText().length());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    link.setSelectionStart(0);
                    link.setSelectionEnd(0);
                }
            });

            hp.add(link, "wrap, growx");
            hp.repaint();
        } else {
            //probably it shouln't happen
            System.out.println("JCloisterZone " + appUpdate.getVersion() + " is avaiable for download.");
            System.out.println(appUpdate.getDescription());
            System.out.println(appUpdate.getDownloadUrl());
        }
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void setShowHistory(boolean showHistory) {
        if (showHistory) {
            gamePanel.getMainPanel().showRecentHistory();
        } else {
            gamePanel.getMainPanel().hideRecentHistory();
        }
        this.showHistory = showHistory;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    //------------------- LEGACY: TODO refactor ---------------

    @Deprecated
    public ControlPanel getControlPanel() {
        return gamePanel == null ? null : gamePanel.getControlPanel();
    }

    @Deprecated
    public GridPanel getGridPanel() {
        if (gamePanel == null || gamePanel.getMainPanel() == null) return null;
        return gamePanel.getMainPanel().getGridPanel();
    }

    @Deprecated
    public MainPanel getMainPanel() {
        if (gamePanel == null) return null;
        return gamePanel.getMainPanel();
    }

    @Deprecated
    public Color getPlayerSecondTunelColor(Player player) {
        //TODO more effective implementation, move it to tunnel capability
        int slotNumber = player.getSlot().getNumber();
        PlayerSlot fakeSlot = new PlayerSlot((slotNumber + 2) % PlayerSlot.COUNT);
        return getConfig().getPlayerColor(fakeSlot).getMeepleColor();
    }



}