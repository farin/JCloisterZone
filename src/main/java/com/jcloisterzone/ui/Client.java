package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameEventListener;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GuiClientStub;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotType;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.rmi.mina.ClientStub;
import com.jcloisterzone.server.Server;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.dialog.GameOverDialog;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ConnectGamePanel;
import com.jcloisterzone.ui.panel.CreateGamePanel;
import com.jcloisterzone.ui.panel.StartPanel;
import com.jcloisterzone.ui.theme.ControlsTheme;
import com.jcloisterzone.ui.theme.FigureTheme;
import com.jcloisterzone.ui.theme.TileTheme;

@SuppressWarnings("serial")
public class Client extends JFrame implements UserInterface, GameEventListener {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final Ini config;
	private final ClientSettings settings;
	private TileTheme tileTheme;
	private FigureTheme figureTheme;
	private ControlsTheme controlsTheme;
	private Color[] playerColors;

	private MenuBar menu;
	private ControlPanel controlPanel;
	private MainPanel mainPanel;

	//private CreateGameDialog gameDialog;
	private CreateGamePanel createGamePanel;
	private DiscardedTilesDialog discardedTilesDialog;

	private Server localServer;
	private ServerIF server;

	private Game game;
	//active player must be cached locally because of game's active player record is changed in other thread immediately
	private Player activePlayer;

	protected ClientStub getClientStub() {
		return (ClientStub) Proxy.getInvocationHandler(server);
	}

	public long getClientId() {
		return getClientStub().getClientId();
	}

	private Locale getLocaleFromConfig() {
		String language = config.get("ui", "locale");
		if (language == null) {
			return Locale.getDefault();
		}
		if (language.contains("_")) {
			String[] tokens = language.split("_", 2);
			return new Locale(tokens[0], tokens[1]);
		}
		return new Locale(language);
	}

	private Color stringToColor(String colorName) {
		if (colorName.startsWith("#")) {
			//RGB format
			int r = Integer.parseInt(colorName.substring(1,3),16);
			int g = Integer.parseInt(colorName.substring(3,5),16);
			int b = Integer.parseInt(colorName.substring(5,7),16);
			return new Color(r,g,b);
		} else {
			//constant format
			java.lang.reflect.Field f;
			try {
				f = Color.class.getField(colorName);
				return (Color) f.get(null);
			} catch (Exception e1) {
				logger.error("Invalid color name in config file: " + colorName);
				return Color.BLACK;
			}
		}
	}

	@Override
	public void setLocale(Locale l) {
		I18nUtils.setLocale(l);
		super.setLocale(l);
	}

	public Client(String configFile) {
		config = new Ini();
		try {
			config.load(Client.class.getClassLoader().getResource(configFile));
		} catch (Exception ex) {
			logger.error("Unable to read config.ini", ex);
			System.exit(1);
		}
		setLocale(getLocaleFromConfig());
		settings = new ClientSettings(config);
		List<String> colorNames = config.get("players").getAll("color");
		playerColors = new Color[colorNames.size()];
		for(int i = 0; i < playerColors.length; i++ ) {
			playerColors[i] = stringToColor(colorNames.get(i));
		}
		tileTheme = new TileTheme(this);
		figureTheme = new FigureTheme(this);
		controlsTheme = new ControlsTheme(this);

		resetWindowIcon();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		menu = new MenuBar(this);
		this.setJMenuBar(menu);

		//Toolkit.getDefaultToolkit().addAWTEventListener(new GlobalKeyListener(), AWTEvent.KEY_EVENT_MASK);

		Container pane = this.getContentPane();
		pane.setLayout(new BorderLayout());
		JPanel envelope = new BackgroundPanel(new GridBagLayout());
		pane.add(envelope, BorderLayout.CENTER);

		StartPanel panel = new StartPanel();
		panel.setClient(this);
		//panel.setPreferredSize(new Dimension(800, 600));
		envelope.add(panel);

		/*controlPanel = new ControlPanel(this);
		pane.add(controlPanel, BorderLayout.EAST);
		gridPanel = new GridPanel(this);
		pane.add(new JScrollPane(gridPanel), BorderLayout.CENTER);*/

		this.pack();
		this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		this.setTitle("JCloisterZone");
		this.setVisible(true);
	}
	
	private void resetWindowIcon() {
		this.setIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
	}

	public Ini getConfig() {
		return config;
	}

	public ClientSettings getSettings() {
		return settings;
	}

	public TileTheme getTileTheme() {
		return tileTheme;
	}

	public FigureTheme getFigureTheme() {
		return figureTheme;
	}

	public ControlsTheme getControlsTheme() {
		return controlsTheme;
	}

	public ServerIF getServer() {
		return server;
	}

	public Game getGame() {
		return game;
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}

	public GridPanel getGridPanel() {
		return mainPanel.getGridPanel();
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	public void cleanContentPane() {
		Container pane = this.getContentPane();
		pane.setVisible(false);
		pane.removeAll();
		if (createGamePanel != null) {
			createGamePanel.disposePanel();
		}
	}

	public void showCreateGamePanel(boolean mutableSlots) {
		Container pane = this.getContentPane();
		cleanContentPane();
		createGamePanel = new CreateGamePanel(this, mutableSlots);
		JPanel envelope = new BackgroundPanel();
		envelope.setLayout(new GridBagLayout()); //to have centered inner panel
		envelope.add(createGamePanel);

		JScrollPane scroll = new JScrollPane(envelope);
		pane.add(scroll, BorderLayout.CENTER);
		pane.setVisible(true);
	}

	public boolean closeGame() {
		return closeGame(false);
	}

	public boolean closeGame(boolean force) {
		if (settings.isConfirmGameClose() && game != null && !(game.getPhase() instanceof GameOverPhase)) {
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
		if (localServer != null) {
			localServer.stop();
			localServer = null;
		}
		server = null;
		activePlayer = null;
		menu.setIsGameRunning(false);
		if (controlPanel != null) {
			controlPanel.closeGame();
			mainPanel.closeGame();
		}
		if (discardedTilesDialog != null) {
			discardedTilesDialog.dispose();
			discardedTilesDialog = null;
			menu.setShowDiscardedEnabled(false);
		}
		return true;
	}

	public void showConnectGamePanel() {
		if (! closeGame()) return;
		
		Container pane = this.getContentPane();
		cleanContentPane();

		JPanel envelope = new BackgroundPanel();
		envelope.setLayout(new GridBagLayout()); //to have centered inner panel
		envelope.add(new ConnectGamePanel(this));

		pane.add(envelope, BorderLayout.CENTER);
		pane.setVisible(true);
	}

	public void setGame(Game game) {
		this.game = game;
		Object clientProxy = Proxy.newProxyInstance(Client.class.getClassLoader(),
				new Class[] { UserInterface.class, GameEventListener.class }, new InvokeInSwingUiAdapter(this));
		game.addUserInterface((UserInterface) clientProxy);
		game.addGameListener((GameEventListener) clientProxy);
	}

	public void connect(InetAddress ia, int port) {
		GuiClientStub handler = new GuiClientStub(this);
		server = (ServerIF) Proxy.newProxyInstance(ServerIF.class.getClassLoader(),
				new Class[] { ServerIF.class }, handler);
	    handler.setServerProxy(server);
	    handler.connect(ia, port);
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
				if (! file.getName().endsWith(".jcz")) {
					file = new File(file.getAbsolutePath() + ".jcz");
				}
				try {
					Snapshot snapshot = new Snapshot(game, getClientId());
					if ("plain".equals(getConfig().get("debug", "save_format"))) {
						snapshot.setGzipOutput(false);
					}
					snapshot.save(file);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private int getServerPort() {
		return config.get("server", "port", int.class);
	}

	public void createGame() {
		if (! closeGame()) return;
		try {
			localServer = new Server(config);
			localServer.start(getServerPort());
			connect(InetAddress.getLocalHost(), getServerPort());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(this, e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
			closeGame(true);
		}
	}

	public void handleLoad() {
		if (! closeGame()) return;
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
					localServer = new Server(new Snapshot(file));
					localServer.start(getServerPort());
					connect(InetAddress.getLocalHost(), getServerPort());
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
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

	public boolean isClientActive() {
		if (activePlayer == null) return false;
		if (activePlayer.getSlot().getType() != SlotType.PLAYER) return false;
		return getClientStub().isLocalPlayer(activePlayer);
	}

	public Player getActivePlayer() {
		return activePlayer;
	}

	private void beep() {
		if (settings.isPlayBeep()) {
			try {
				AudioInputStream beepStream = AudioSystem.getAudioInputStream(Client.class.getClassLoader().getResource("beep.wav").openStream());
				Clip c = AudioSystem.getClip();
				c.open(beepStream);
				c.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void showWarning(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
		
	}

	@Override
	public void updateCustomRule(CustomRule rule, Boolean enabled) {
		createGamePanel.updateCustomRule(rule, enabled);
	}

	@Override
	public void updateExpansion(Expansion expansion, Boolean enabled) {
		createGamePanel.updateExpansion(expansion, enabled);
	}

	@Override
	public void updateSlot(PlayerSlot slot) {
		createGamePanel.updateSlot(slot);
	}

	@Override
	public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
		createGamePanel.updateSupportedExpansions(expansions);
	}

	@Override
	public void started(Snapshot snapshot) {
		cleanContentPane();

		Container pane = getContentPane();


		pane.setLayout(new BorderLayout());
		controlPanel = new ControlPanel(this);
		JScrollPane ctrlScroll = new JScrollPane(controlPanel);
		ctrlScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ctrlScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.add(ctrlScroll, BorderLayout.EAST);
		//pane.add(controlPanel, BorderLayout.EAST);
		mainPanel = new MainPanel(this);

		JScrollPane scroll = new JScrollPane(mainPanel);
		scroll.setWheelScrollingEnabled(false);
		pane.add(scroll, BorderLayout.CENTER);
		pane.setVisible(true);

		mainPanel.started(snapshot);
		controlPanel.started();
		menu.setZoomInEnabled(true);
		menu.setZoomOutEnabled(true);
		menu.setIsGameRunning(true);
	}

	@Override
	public void playerActivated(Player turnPlayer, Player activePlayer) {
		this.activePlayer = activePlayer;
		controlPanel.playerActivated(turnPlayer, activePlayer);

		//TODO better image quality ?
		Color c = getPlayerColor(activePlayer);
		Image image = getFigureTheme().getFigureImage(SmallFollower.class, c, null);
		setIconImage(image);
	}

	@Override
	public void tileDrawn(Tile tile) {
		clearActions();
		controlPanel.tileDrawn(tile);
	}

	@Override
	public void tileDiscarded(String tileId) {
		if (discardedTilesDialog == null) {
			discardedTilesDialog = new DiscardedTilesDialog(this);
			menu.setShowDiscardedEnabled(true);
		}
		discardedTilesDialog.addTile(tileId);
		discardedTilesDialog.setVisible(true);
	}

	@Override
	public void tilePlaced(Tile tile) {
		mainPanel.tilePlaced(tile);
		controlPanel.tilePlaced(tile);
	}

	@Override
	public void dragonMoved(Position p) {
		mainPanel.dragonMoved(p);
	}

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		clearActions();
		DragonLayer dragonDecoration = getGridPanel().findDecoration(DragonLayer.class);
		dragonDecoration.setMoves(movesLeft);
		getGridPanel().repaint();
		if (isClientActive()) {
			getGridPanel().addLayer(new DragonAvailableMove(getGridPanel(), positions));
			beep();
		}
	}

	@Override
	public void fairyMoved(Position p) {
		mainPanel.fairyMoved(p);
	}

	@Override
	public void towerIncreased(Position p, Integer height) {
		clearActions();
		mainPanel.towerIncreased(p, height);
	}

	@Override
	public void ransomPaid(Player from, Player to, Follower f) {
		controlPanel.repaint();
	}

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		clearActions();
		controlPanel.tileDrawn(game.getTilePack().getAbbeyTile());
		if (isClientActive()) {
			beep();
			controlPanel.selectAbbeyPlacement(positions);
		}
		mainPanel.selectTilePlacement(positions);
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		clearActions();
		Set<Position> positions = placements.keySet();
		if (isClientActive()) {
			beep();
			controlPanel.selectTilePlacement(positions);
		}
		mainPanel.selectTilePlacement(positions);
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		if (isClientActive()) {
			controlPanel.selectAction(actions, true);
		}
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		controlPanel.selectAction(Collections.<PlayerAction>singletonList(action), false);
	}
	
	@Override
	public void tunnelPiecePlaced(Player player, Position p, Location d, boolean isSecondPiece) {
		mainPanel.tunnelPiecePlaced(player, p, d, isSecondPiece);
	}


	@Override
	public void gameOver() {
		resetWindowIcon();
		closeGame(true);
		new GameOverDialog(this);
	}

	private void clearActions() {
		if (controlPanel.getActionPanel().getActions() != null) {
			controlPanel.clearActions();
		}
	}

	public DiscardedTilesDialog getDiscardedTilesDialog() {
		return discardedTilesDialog;
	}

	//------------------ Meeple events -----------


	@Override
	public void deployed(Meeple m) {
		mainPanel.deployed(m);
	}

	@Override
	public void undeployed(Meeple m) {
		mainPanel.undeployed(m);
	}
	
	@Override
	public void bridgeDeployed(Position pos, Location loc) {
		mainPanel.bridgeDeployed(pos, loc);	
	}

	// ------------------ Feature evnts ----------

	@Override
	public void completed(Completable feature, CompletableScoreContext ctx) { }

	@Override
	public void scored(Feature feature, int points, String label, Meeple meeple, boolean finalScoring) {
		mainPanel.scored(feature, label, meeple, finalScoring);
		controlPanel.getPlayersPanel().repaint();
	}

	@Override
	public void scored(Position position, Player player, int points, String label, boolean finalScoring) {
		mainPanel.scored(position, player, label, finalScoring);
		controlPanel.getPlayersPanel().repaint();
	}


	//------------------- LEGACY: TODO refactor ---------------
	//TODO move getColor on player - ale je to potreba i u slotu, pozor na to


	public Color getPlayerSecondTunelColor(Player player) {
		int slotNumber = player.getSlot().getNumber();
		return playerColors[(slotNumber + 2) % playerColors.length];
	}

	public Color getPlayerColor(Player player) {
		return playerColors[player.getSlot().getNumber()];
	}

	public Color getPlayerColor(PlayerSlot playerSlot) {
		return playerColors[playerSlot.getNumber()];
	}

	public Color getPlayerColor() {
		return playerColors[game.getActivePlayer().getSlot().getNumber()];
	}

}
