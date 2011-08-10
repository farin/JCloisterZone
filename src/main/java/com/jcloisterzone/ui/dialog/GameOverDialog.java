package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.Client;

public class GameOverDialog extends JDialog {

	private final Client client;

	public GameOverDialog(Client client) {
		super(client);
		this.client = client;

		setTitle(_("Game overview"));
		Point p = client.getLocation();
		setLocation(p.x+200,p.y+150);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container pane = getContentPane();
		pane.setLayout(new MigLayout("", "[]", "[][]10[]10[]20[][][][]20[][]20[][][]"));

		pane.add(new JLabel(_("Player")), getLegendSpec(0, 1));
		pane.add(new JLabel(_("Rank")), getLegendSpec(0, 2));
		pane.add(new JLabel(_("Total points")), getLegendSpec(0, 3));

		pane.add(new JLabel(_("Roads")), getLegendSpec(0, 4));
		pane.add(new JLabel(_("Cities")), getLegendSpec(0, 5));
		pane.add(new JLabel(_("Cloisters")), getLegendSpec(0, 6));
		pane.add(new JLabel(_("Farms")), getLegendSpec(0, 7));

		pane.add(new JLabel(_("The biggest city")), getLegendSpec(0, 8));
		pane.add(new JLabel(_("The Longest road")), getLegendSpec(0, 9));

		pane.add(new JLabel(_("Trade goods")), getLegendSpec(0, 10));
		pane.add(new JLabel(_("Fairy")), getLegendSpec(0, 11));
		pane.add(new JLabel(_("Tower ransom")), getLegendSpec(0, 12));

		int i = 0;
		Player[] players = getSortedPlayers().toArray(new Player[client.getGame().getAllPlayers().length]);
		for(Player player : players) {
			Color color = client.getPlayerColor(player);
			Image img = client.getFigureTheme().getFigureImage(SmallFollower.class, color, null);
			Icon icon = new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
			pane.add(new JLabel(icon, SwingConstants.CENTER), getSpec(i, 0));
			pane.add(new JLabel(player.getNick(), SwingConstants.CENTER), getSpec(i, 1));

			pane.add(new JLabel(getRank(players, i), SwingConstants.CENTER), getSpec(i, 2)); //rank TODO draw
			pane.add(new JLabel("" +player.getPoints(), SwingConstants.CENTER), getSpec(i, 3));

			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.ROAD), SwingConstants.CENTER), getSpec(i, 4));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.CITY), SwingConstants.CENTER), getSpec(i, 5));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.CLOISTER), SwingConstants.CENTER), getSpec(i, 6));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.FARM), SwingConstants.CENTER), getSpec(i, 7));

			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.BIGGEST_CITY), SwingConstants.CENTER), getSpec(i, 8));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.LONGEST_ROAD), SwingConstants.CENTER), getSpec(i, 9));

			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.TRADE_GOODS), SwingConstants.CENTER), getSpec(i, 10));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.FAIRY), SwingConstants.CENTER), getSpec(i, 11));
			pane.add(new JLabel("" +player.getPointsInCategory(PointCategory.TOWER_RANSOM), SwingConstants.CENTER), getSpec(i, 12));
			i++;
		}

		pack();
		setVisible(true);
	}

	private String getRank(Player[] players, int i) {
		int endrank = i+1;
		while(i > 0 && players[i-1].getPoints() == players[i].getPoints()) i--; //find start of group
		while(endrank < players.length) {
			if (players[endrank].getPoints() != players[i].getPoints()) break;
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
		List<Player> players = Arrays.asList(client.getGame().getAllPlayers());
		Collections.sort(players, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				if (o1.getPoints() < o2.getPoints()) return 1;
				if (o1.getPoints() > o2.getPoints()) return -1;
				return o1.getNick().compareToIgnoreCase(o2.getNick());
			}
		});
		return players;
	}

}
