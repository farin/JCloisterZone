package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;
import static com.jcloisterzone.ui.panel.GameOverPanel.getPlaytimeByPlayer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TradeGoodsCapability;
import com.jcloisterzone.game.capability.WindRoseCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

import net.miginfocom.swing.MigLayout;

public class GameOverPanel extends JPanel {

    public static final ImageIcon COLLAPSE_ICON = UiUtils.scaleImageIcon("sysimages/chevron-left-gray.png", 20, 20);
    public static final ImageIcon EXPAND_ICON = UiUtils.scaleImageIcon("sysimages/chevron-right-gray.png", 20, 20);

    private final Client client;
    private final Game game;

    private JLabel collapseIcon;
    private boolean collapsed;

    public GameOverPanel(Client client, final GameController gc, boolean showPlayAgain) {
        this.client = client;
        this.game = gc.getGame();

        setOpaque(true);
        setBackground(client.getTheme().getSemiTransparentBg());
        setLayout(new MigLayout("ins 20", "[][grow]", "[]20[]"));

        add(new PointStatsPanel(), "sx 2, wrap, hidemode 3");
        JButton btn;
        btn = new JButton(_tr("Leave game"));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.leaveGame();
            }
        });
        add(btn, "hidemode 3");

        if (showPlayAgain) {
            btn = new JButton(_tr("Play again"));
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GameOverPanel.this.client.createGame(game);
                }
            });
            add(btn, "gapleft 5, hidemode 3");
        }

        collapseIcon = new JLabel(COLLAPSE_ICON);
        collapseIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setCollapsed(!isCollapsed());
                    repaint();
                }
            }
        });
        add(collapseIcon, "pos 4 4 20 20");
    }



    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        for (Component c : getComponents()) {
            if (c != collapseIcon) {
                c.setVisible(!collapsed);
            }
        }
        collapseIcon.setIcon(collapsed ? EXPAND_ICON : COLLAPSE_ICON);
    }

    class PointStatsPanel extends JPanel {

        public PointStatsPanel() {
            GameState state = game.getState();
            CapabilitiesState capabilities = state.getCapabilities();

            //setTitle(_tr("Game overview"));
            boolean hasBazaars = capabilities.contains(BazaarCapability.class) && !state.getBooleanValue(Rule.BAZAAR_NO_AUCTION);

            StringBuilder rowSpec = new StringBuilder("[][]10[][]10[][]20[][][][]");
            if (capabilities.contains(CastleCapability.class)) rowSpec.append("[]");
            rowSpec.append("20"); //gap
            if (capabilities.contains(KingAndRobberBaronCapability.class)) rowSpec.append("[][]20");
            if (capabilities.contains(TradeGoodsCapability.class)) rowSpec.append("[]");
            if (capabilities.contains(GoldminesCapability.class)) rowSpec.append("[]");
            if (capabilities.contains(SheepCapability.class)) rowSpec.append("[]");
            if (capabilities.contains(FairyCapability.class)) rowSpec.append("[]");
            if (capabilities.contains(TowerCapability.class)) rowSpec.append("[]");
            if (hasBazaars) rowSpec.append("[]");
            if (capabilities.contains(WindRoseCapability.class)) rowSpec.append("[]");

            setOpaque(false);
            setLayout(new MigLayout("ins 0", "", rowSpec.toString()));
            int gridx = 0, gridy = 1;

            add(new JLabel(_tr("Player")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Rank")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Total points")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Tiles placed")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Time consumed:(Player/Game)")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Time consumed:(Percentage)")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Roads")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Cities")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Cloisters")), getLegendSpec(0, gridy++));
            add(new JLabel(_tr("Farms")), getLegendSpec(0, gridy++));
            if (capabilities.contains(CastleCapability.class)) {
                add(new JLabel(_tr("Castles")), getLegendSpec(0, gridy++));
            }

            if (capabilities.contains(KingAndRobberBaronCapability.class)) {
                add(new JLabel(_tr("The biggest city")), getLegendSpec(0, gridy++));
                add(new JLabel(_tr("The longest road")), getLegendSpec(0, gridy++));
            }

            if (capabilities.contains(TradeGoodsCapability.class)) {
                add(new JLabel(_tr("Trade goods")), getLegendSpec(0, gridy++));
            }
            if (capabilities.contains(GoldminesCapability.class)) {
                add(new JLabel(_tr("Gold")), getLegendSpec(0, gridy++));
            }
            if (capabilities.contains(SheepCapability.class)) {
                add(new JLabel(_tr("Sheep")), getLegendSpec(0, gridy++));
            }
            if (capabilities.contains(FairyCapability.class)) {
                add(new JLabel(_tr("Fairy")), getLegendSpec(0, gridy++));
            }
            if (capabilities.contains(TowerCapability.class)) {
                add(new JLabel(_tr("Tower ransom")), getLegendSpec(0, gridy++));
            }
            if (hasBazaars) {
                add(new JLabel(_tr("Bazaars")), getLegendSpec(0, gridy++));
            }
            if (capabilities.contains(WindRoseCapability.class)) {
                add(new JLabel(_tr("Wind rose")), getLegendSpec(0, gridy++));
            }

            Player[] players = getSortedPlayers().toArray(new Player[state.getPlayers().length()]);
            // calculate the full time of the current played game
            int totalSecondsPlayed = 0;
            for (Player player : players) {
                totalSecondsPlayed = totalSecondsPlayed + getPlaytimeByPlayer(game, player);
            }
            for (Player player : players) {
                gridy = 0;
                Color color = player.getColors().getMeepleColor();
                Image img = client.getResourceManager().getLayeredImage(new LayeredImageDescriptor(SmallFollower.class, color));
                Icon icon = new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                add(new JLabel(icon, SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel(player.getNick(), SwingConstants.CENTER), getSpec(gridx, gridy++));

                add(new JLabel(getRank(players, gridx), SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel("" +player.getPoints(state), SwingConstants.CENTER), getSpec(gridx, gridy++));

                int tilesPlaced = state.getEvents().filter(ev -> ev instanceof TilePlacedEvent && Integer.valueOf(player.getIndex()).equals(ev.getMetadata().getTriggeringPlayerIndex())).size();
                add(new JLabel("" +tilesPlaced, SwingConstants.CENTER), getSpec(gridx, gridy++));

                Playtime playtime = new Playtime(game, player, totalSecondsPlayed);
                add(new JLabel(formatPlaytimeString(playtime), SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel(formatPercentageString(playtime), SwingConstants.CENTER), getSpec(gridx, gridy++));

                add(new JLabel("" +player.getPointsInCategory(state, PointCategory.ROAD), SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel("" +player.getPointsInCategory(state, PointCategory.CITY), SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel("" +player.getPointsInCategory(state, PointCategory.CLOISTER), SwingConstants.CENTER), getSpec(gridx, gridy++));
                add(new JLabel("" +player.getPointsInCategory(state, PointCategory.FARM), SwingConstants.CENTER), getSpec(gridx, gridy++));
                if (capabilities.contains(CastleCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.CASTLE), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }

                if (capabilities.contains(KingAndRobberBaronCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.BIGGEST_CITY), SwingConstants.CENTER), getSpec(gridx, gridy++));
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.LONGEST_ROAD), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }

                if (capabilities.contains(TradeGoodsCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.TRADE_GOODS), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (capabilities.contains(GoldminesCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.GOLD), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (capabilities.contains(SheepCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.SHEEP), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (capabilities.contains(FairyCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.FAIRY), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (capabilities.contains(TowerCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.TOWER_RANSOM), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (hasBazaars) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.BAZAAR_AUCTION), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                if (capabilities.contains(WindRoseCapability.class)) {
                    add(new JLabel("" +player.getPointsInCategory(state, PointCategory.WIND_ROSE), SwingConstants.CENTER), getSpec(gridx, gridy++));
                }
                gridx++;
            }
        }

        private String getRank(Player[] players, int i) {
            GameState state = game.getState();
            int endrank = i+1;
            while(i > 0 && players[i-1].getPoints(state) == players[i].getPoints(state)) i--; //find start of group
            while(endrank < players.length) {
                if (players[endrank].getPoints(state) != players[i].getPoints(state)) break;
                endrank++;
            }
            if (endrank == i+1) {
                return "" + endrank;
            }
            return (i+1) + " - " + (endrank);
        }

        private String getLegendSpec(int x, int y) {
            return "cell " + x + " " + y + ", width 170::";
        }

        private String getSpec(int x, int y) {
            return "cell " + x + " " + y + ", width 120::, right";
        }

        private List<Player> getSortedPlayers() {
            GameState state = game.getState();
            List<Player> players = state.getPlayers().getPlayers().toJavaList();
            Collections.sort(players, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    if (o1.getPoints(state) < o2.getPoints(state)) return 1;
                    if (o1.getPoints(state) > o2.getPoints(state)) return -1;
                    return o1.getNick().compareToIgnoreCase(o2.getNick());
                }
            });
            return players;
        }
    }

    static String formatPlaytimeString(Playtime playtime) {
        return String.format("%d:%02d:%02d/\n%d:%02d:%02d",
                playtime.getHours(), playtime.getMinutes(), playtime.getSeconds(),
                playtime.getTotalHours(), playtime.getTotalMinutes(), playtime.getTotalSeconds());
    }

    static String formatPercentageString(Playtime playtime) {
        return String.format("%.0f%%", playtime.getPercentage());
    }

    /**
     * @return playtime in seconds
     */
    static int getPlaytimeByPlayer(Game game, Player player) {
        return (int) (game.getClocks().get(player.getIndex()).getTime(game.getClockStart()) / 1000);
    }
}



class Playtime {
    private int hours;
    private int minutes;
    private int seconds;
    private int totalHours;
    private int totalMinutes;
    private int totalSeconds;
    private float percentage;

    Playtime(Game game, Player player, int totalRawSeconds) {
        int playerRawSeconds = getPlaytimeByPlayer(game, player);
        this.hours = playerRawSeconds / 3600;
        this.minutes = (playerRawSeconds % 3600) / 60;
        this.seconds = playerRawSeconds % 60;
        this.totalHours = totalRawSeconds / 3600;
        this.totalMinutes = (totalRawSeconds % 3600) / 60;
        this.totalSeconds = totalRawSeconds % 60;
        this.percentage = ((float) playerRawSeconds / (float) totalRawSeconds) * 100;
    }

    int getHours() {
        return hours;
    }

    int getMinutes() {
        return minutes;
    }

    int getSeconds() {
        return seconds;
    }

    int getTotalHours() {
        return totalHours;
    }

    int getTotalMinutes() {
        return totalMinutes;
    }

    int getTotalSeconds() {
        return totalSeconds;
    }

    float getPercentage() {
        return percentage;
    }
}
