package com.jcloisterzone.ui.legacy;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.game.expansion.KingAndScoutGame;
import com.jcloisterzone.game.expansion.TowerGame;
import com.jcloisterzone.game.expansion.TradersAndBuildersGame;
import com.jcloisterzone.ui.Client;


@SuppressWarnings("serial")
public class PlayerPanel extends JPanel { //TODO JLabel

	private enum PanelImages {
		BASIC("panel_basic.png"),
		BASIC_BORDER("panel_basic_r.png"),
		DOUBLE("panel_double.png"),
		DOUBLE_BORDER("panel_double_r.png"),
		BUILDER("panel_builder.png"),
		BUILDER_BORDER("panel_builder_r.png"),
		PIG("panel_pig.png"),
		PIG_BORDER("panel_pig_r.png"),
		TOWER_PIECE("panel_tower.png"),
		BARN("panel_barn.png"),
		BARN_BORDER("panel_barn_r.png"),
		MAYOR("panel_mayor.png"),
		MAYOR_BORDER("panel_mayor_r.png"),
		WAGON("panel_wagon.png"),
		WAGON_BORDER("panel_wagon_r.png"),
		CASTLE("panel_castle.png"),
		BRIDGE("panel_bridge.png"),

		RESOURCE_CLOTH("res_cloth.png"),
		RESOURCE_GRAIN("res_grain.png"),
		RESOURCE_WINE("res_wine.png"),

		ACTIVE("active.png");

		public String path;

		PanelImages(String p) {
			path = p;
		}

	}

	private static final ImageIcon bonusKing = createIcon("bonus_king.png");
	private static final ImageIcon bonusRobberBaron = createIcon("bonus_footpad.png");
	private static final ImageIcon bonusBoth = createIcon("bonus_both.png");


	//private ControlPanel controlPanel;

	private final Client client;

	public static int getHeight(Game game) {
		int height = BASIC_HEIGHT;
		if (game.hasExpansion(Expansion.TRADERS_AND_BUILDERS)) height += EXPANSION_EXTRA_HEIGHT;			
		if (game.hasExpansion(Expansion.TOWER)) height += EXPANSION_EXTRA_HEIGHT;			
		if (game.hasExpansion(Expansion.ABBEY_AND_MAYOR)) height += EXPANSION_EXTRA_HEIGHT;
		if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) height += EXPANSION_EXTRA_HEIGHT;
		return height;
	}

	private Player p;
	//private boolean isActive;
	private Color color;

	private Map<Object,Image> figures = Maps.newHashMap();

	//klikaci oblast kde jsou figurky pro vraceni
	//private int ransomBoxBasic, ransomBoxBasicEnd, ransomBoxDouble, ransomBoxDoubleEnd;
	//private PayRansomListener payRansomListener;
	private Map<Rectangle, Class<? extends Follower>> ransomClickable = Maps.newHashMap();


	private static Font resourceFont = new Font("Helvecia", Font.PLAIN, 18);
	private static Font nickFont = new Font("Helvecia", Font.BOLD, 14);
	private static Font pointFont = new Font("Helvecia", Font.BOLD, 30);

	private Game game;

	private JLabel kingAndScout;

	public PlayerPanel(Client client, Player p) {
		this.client = client;
		this.color = client.getPlayerColor(p);
		this.game = client.getGame();
		this.p = p;

		ImageFilter colorfilter = new FigureImageFilter(color);

		initFigure(PanelImages.BASIC,colorfilter);
		initImage(PanelImages.BASIC_BORDER);
		initFigure(PanelImages.DOUBLE,colorfilter);
		initImage(PanelImages.DOUBLE_BORDER);
		initFigure(PanelImages.BUILDER,colorfilter);
		initImage(PanelImages.BUILDER_BORDER);
		initFigure(PanelImages.PIG,colorfilter);
		initFigure(PanelImages.BARN,colorfilter);
		initImage(PanelImages.BARN_BORDER);
		initFigure(PanelImages.MAYOR,colorfilter);
		initImage(PanelImages.MAYOR_BORDER);
		initFigure(PanelImages.WAGON,colorfilter);
		initImage(PanelImages.WAGON_BORDER);
		initImage(PanelImages.PIG_BORDER);
		initImage(PanelImages.TOWER_PIECE);
		initImage(PanelImages.CASTLE);
		initImage(PanelImages.BRIDGE);

		initImage(PanelImages.RESOURCE_CLOTH);
		initImage(PanelImages.RESOURCE_GRAIN);
		initImage(PanelImages.RESOURCE_WINE);

		initImage(PanelImages.ACTIVE);
		
		TOWER_Y = BASIC_HEIGHT;
		ABBEY_Y = BASIC_HEIGHT;
		BRIDGES_Y = BASIC_HEIGHT;
		
		if (game.hasExpansion(Expansion.TRADERS_AND_BUILDERS)) {
			TOWER_Y += EXPANSION_EXTRA_HEIGHT;
			ABBEY_Y += EXPANSION_EXTRA_HEIGHT;
			BRIDGES_Y += EXPANSION_EXTRA_HEIGHT;
		}
		
		if (game.hasExpansion(Expansion.TOWER)) {
			ABBEY_Y += EXPANSION_EXTRA_HEIGHT;
			BRIDGES_Y += EXPANSION_EXTRA_HEIGHT;
			
			addMouseListener(new PayRansomListener());
		}
		
		if (game.hasExpansion(Expansion.ABBEY_AND_MAYOR)) {
			BRIDGES_Y += EXPANSION_EXTRA_HEIGHT;
		}

		setLayout(null); //because of kignAndScout icon and absolute position!
		//setLayout(new MigLayout());
		if (game.hasExpansion(Expansion.KING_AND_SCOUT)) {
			kingAndScout = new JLabel();
			kingAndScout.setBounds(POINTS_BORDER_X + 5, SPECIAL_Y, 30, 30);
			//kingAndScout.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			add(kingAndScout);
		}

	}

	private void initFigure(PanelImages pi, ImageFilter filter) {
		Image img = createImage(new FilteredImageSource(
				new ImageIcon(
						PlayerPanel.class.getClassLoader().getResource("sysimages/"+pi.path)
				).getImage().getSource(),
				filter
		));
		trackAndPut(pi, img);

	}

	private void initImage(PanelImages pi) {
		Image img = new ImageIcon(
					PlayerPanel.class.getClassLoader().getResource("sysimages/"+pi.path)
				).getImage();
		//trackAndPut(pi, img);
		figures.put(pi,img);
	}

	static private ImageIcon createIcon(String path) {
		return new ImageIcon(
					PlayerPanel.class.getClassLoader().getResource("sysimages/"+path));
	}

	private void trackAndPut(PanelImages pi, Image img) {
		try {
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(img, 0);
			tracker.waitForID(0);
		} catch (Exception ex) {
			//do nothing
		}
		figures.put(pi,img);
	}

	private final static int OUT_BORDER = 3;

	private final static int NICK_X = 20;
	private final static int NICK_Y = 19;
	private final static int NICK_UNDER = 25;

	private final static int FIGURES_X = 11;
	private final static int FIGURES_Y = 29;
	private final static int FIGURE_WIDTH = 19;
	private final static int TOWER_FIGURE_WIDTH = 20;
	//private final static int DOUBLE_FIGURE_WIDTH = 23;

	//private final static int SPECIAL_X = 135;
	private final static int SPECIAL_Y = 54;

	private final static int POINTS_X = 20;
	private final static int POINTS_Y = 84;
	private final static int POINTS_BORDER_X = 80;

	private final static int RESOURCES_X = 11;
	private final static int RESOURCES_Y = 95;

	private final static int BASIC_HEIGHT = 97;
	private final static int EXPANSION_EXTRA_HEIGHT = 28;
	private int TOWER_Y, ABBEY_Y, BRIDGES_Y; //depends on expansions
	private final static int TOWER_FIRST_CAPTURED_X = 30 + FIGURE_WIDTH;

	@Override
	public void paintComponent(Graphics g) {
		//mozna by to chtelo refactoring
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		boolean isActive = game.getActivePlayer().getIndex() == p.getIndex();
		boolean playerTurn = game.getTurnPlayer().getIndex() == p.getIndex();

		setBackground(isActive ? Color.LIGHT_GRAY : null);

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), OUT_BORDER);
		g2.fillRect(0, getHeight()-OUT_BORDER-1, getWidth(), OUT_BORDER);
		g2.drawLine(0, NICK_UNDER, getWidth()-1, NICK_UNDER);

		g2.drawLine(0, SPECIAL_Y, POINTS_BORDER_X, SPECIAL_Y);
		g2.drawLine(POINTS_BORDER_X, SPECIAL_Y, POINTS_BORDER_X, RESOURCES_Y - 4);
		g2.drawLine(0, RESOURCES_Y - 4, POINTS_BORDER_X, RESOURCES_Y - 4);

		//resources
		if (game.hasExpansion(Expansion.TRADERS_AND_BUILDERS)) {
			g2.drawImage(figures.get(PanelImages.RESOURCE_CLOTH), RESOURCES_X, RESOURCES_Y,null);
			g2.drawImage(figures.get(PanelImages.RESOURCE_GRAIN), RESOURCES_X + 26 + 30, RESOURCES_Y,null);
			g2.drawImage(figures.get(PanelImages.RESOURCE_WINE), RESOURCES_X + 42 + 60, RESOURCES_Y,null);
			g2.setFont(resourceFont);
			TradersAndBuildersGame tbGame = game.getTradersAndBuildersGame();
			g2.drawString(tbGame.getTradeResources(p, TradeResource.CLOTH) + "", RESOURCES_X + 26 + 10, RESOURCES_Y + 16);
			g2.drawString(tbGame.getTradeResources(p, TradeResource.GRAIN) + "", RESOURCES_X + 42 + 40, RESOURCES_Y + 16);
			g2.drawString(tbGame.getTradeResources(p, TradeResource.WINE) + "", RESOURCES_X + 59 + 70, RESOURCES_Y + 16);
		}

		if (game.hasExpansion(Expansion.TOWER)) {
			TowerGame tg = game.getTowerGame();

			ransomClickable.clear();
			g2.setFont(resourceFont);
			int towerPieces = game.getTowerGame().getTowerPieces(p);
			g2.drawString((towerPieces < 10 ? " ":"") + towerPieces + "", 5, TOWER_Y + 16);
			g2.drawImage(figures.get(PanelImages.TOWER_PIECE), 30, TOWER_Y, null);

			//TODO comment bez nahrady - captured v panelu

			List<Follower> capturedFigures = tg.getPrisoners().get(p);
			int x = TOWER_FIRST_CAPTURED_X;
			for(Follower f : capturedFigures) {
				int playerIndex = f.getPlayer().getIndex();
				boolean canPayRansom = client.isClientActive() &&
								client.getGame().getTurnPlayer().getIndex() == playerIndex &&
								! client.getGame().getTowerGame().isRansomPaidThisTurn();

				Color color = client.getPlayerColor(p);
				Image figImage = client.getFigureTheme().getFigureImage(f, color);
				//Image img = figImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
				//img = new ImageIcon(img).getImage();

				g2.drawImage(figImage, x, TOWER_Y, TOWER_FIGURE_WIDTH, TOWER_FIGURE_WIDTH, null);
				if (canPayRansom) {
					g2.setColor(Color.BLACK);
					g2.drawRect(x, TOWER_Y-1, TOWER_FIGURE_WIDTH, TOWER_FIGURE_WIDTH);
					ransomClickable.put(new Rectangle(x, TOWER_Y, TOWER_FIGURE_WIDTH, TOWER_FIGURE_WIDTH), f.getClass());
				}
				x += TOWER_FIGURE_WIDTH + 2;

			}
		}
		
		if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
			BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
			
			g2.setFont(resourceFont);
			int castles  = bcb.getPlayerCastles(p);
			g2.drawString(castles + "", 5, BRIDGES_Y + 16);
			g2.drawImage(figures.get(PanelImages.CASTLE), 20, BRIDGES_Y, null);
			int bridges  = bcb.getPlayerBridges(p);
			g2.drawString(bridges + "", 80, BRIDGES_Y + 16);
			g2.drawImage(figures.get(PanelImages.BRIDGE), 95, BRIDGES_Y, null);
		}

		g2.setColor(Color.WHITE);
		g2.fillRect(0, OUT_BORDER, getWidth(),NICK_UNDER - OUT_BORDER);
		g2.fillRect(0, SPECIAL_Y+1, POINTS_BORDER_X, RESOURCES_Y - SPECIAL_Y - 5);

		if (playerTurn) {
			g2.drawImage(figures.get(PanelImages.ACTIVE),2,OUT_BORDER + 2,null);
		}

		if (color.equals(Color.YELLOW)) {
			g2.setColor(new Color(255,180,0));
		} else {
			g2.setColor(color);
		}

		int i = 0;
		for(Follower f : p.getUndeployedFollowers()) {
			if (f instanceof SmallFollower) {
				g2.drawImage(figures.get(PanelImages.BASIC),FIGURES_X+i*FIGURE_WIDTH,FIGURES_Y,null);
				g2.drawImage(figures.get(PanelImages.BASIC_BORDER),FIGURES_X+i*FIGURE_WIDTH,FIGURES_Y,null);
				i++;
			}
		}

		drawSpecialFigure(g2, BigFollower.class, FIGURES_X+7*FIGURE_WIDTH, FIGURES_Y, PanelImages.DOUBLE, PanelImages.DOUBLE_BORDER);
		drawSpecialFigure(g2, Builder.class, getWidth() - 45, SPECIAL_Y, PanelImages.BUILDER, PanelImages.BUILDER_BORDER);
		drawSpecialFigure(g2, Pig.class, getWidth() - 25, SPECIAL_Y, PanelImages.PIG, PanelImages.PIG_BORDER);
		drawSpecialFigure(g2, Barn.class, FIGURES_X, ABBEY_Y, PanelImages.BARN, PanelImages.BARN_BORDER);
		drawSpecialFigure(g2, Mayor.class, FIGURES_X + 35, ABBEY_Y, PanelImages.MAYOR, PanelImages.MAYOR_BORDER);
		drawSpecialFigure(g2, Wagon.class, FIGURES_X + 60, ABBEY_Y, PanelImages.WAGON, PanelImages.WAGON_BORDER);

		if (game.hasExpansion(Expansion.KING_AND_SCOUT)) {
			KingAndScoutGame ks = game.getKingAndScoutGame();
			if (p == ks.getRobberBaron() && p == ks.getKing()) {
				g2.drawImage(bonusBoth.getImage(), POINTS_BORDER_X + 5, SPECIAL_Y, this);
				kingAndScout.setToolTipText(
						"<html>" +
						_("The biggest city size") + " " + ks.getBiggestCitySize() + "<br>" +
						_("Number of completed cities") + " " + ks.getCompletedCities() + "<br><br>" +
						_("The longest road length") + " " + ks.getLongestRoadLength() + "<br>" +
						_("Number of completed roads") + " " + ks.getCompletedRoads() +
						"</html>"
				);
			} else if (p == ks.getRobberBaron()) {
				g2.drawImage(bonusRobberBaron.getImage(), POINTS_BORDER_X + 5, SPECIAL_Y, this);
				kingAndScout.setToolTipText(
						"<html>" +
						_("The longest road length") + " " + ks.getLongestRoadLength() + "<br>" +
						_("Number of completed roads") + " " + ks.getCompletedRoads() +
						"</html>"
					);

			} else if (p == ks.getKing()) {
				g2.drawImage(bonusKing.getImage(), POINTS_BORDER_X + 5, SPECIAL_Y, this);
				kingAndScout.setToolTipText(
						"<html>" +
						_("The biggest city size") + " " + ks.getBiggestCitySize() + "<br>" +
						_("Number of completed cities") + " " + ks.getCompletedCities() +
						"</html>"
					);
			} else {
				kingAndScout.setToolTipText(null);
			}
		}

		g2.setFont(nickFont);
		g2.drawString(p.getNick(), NICK_X, NICK_Y);

		g2.setFont(pointFont);
		g2.drawString(p.getPoints()+"",POINTS_X,POINTS_Y);
	}

	@SuppressWarnings("unchecked")
	private void drawSpecialFigure(Graphics2D g2, Class<? extends Meeple> ft, int offsetx, int offsety, PanelImages avail, PanelImages border) {
		boolean hasMeeple;
		if (Follower.class.isAssignableFrom(ft)) {
			hasMeeple = p.hasFollower((Class<? extends Follower>) ft);
		} else {
			hasMeeple = p.hasSpeialMeeple((Class<? extends Special>) ft);
		}
		if (hasMeeple) {
			g2.drawImage(figures.get(avail),offsetx,offsety,null);
			g2.drawImage(figures.get(border),offsetx,offsety,null);
		}
	}

	class PayRansomListener extends MouseAdapter {
		/* nez delat nejaky buttony bude jednodussi listener a odpocet ze
		 * souradnic, zejmena protoze figurky se kresli ze dvou obrazku
		 * a buttony nebo JLabely by byly take slozitejsi
		 */

		@Override
		public void mouseClicked(MouseEvent e) {
			//vyska figurky je 19px
			if (e.getButton() != MouseEvent.BUTTON1) return;
			for(Entry<Rectangle, Class<? extends Follower>> entry : ransomClickable.entrySet()) {
				Rectangle rect = entry.getKey();
				if (rect.contains(e.getX(), e.getY())) {
					client.getServer().payRansom(p.getIndex(), entry.getValue());
				}
			}
		}
	}


}
