package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.google.common.collect.Collections2;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.JLabelWithAntialiasing;

public class ControlPanel extends JPanel {

	private final Client client;

	private NextSquare nextTileLabel;
	private JLabelWithAntialiasing packSize;
	private JButton buttonNextTurn;
	private ActionPanel actionPanel;
	private PlayersPanel playersPanel;

	public static final int PANEL_WIDTH = 170;
	private static Font cardFont = new Font("Helvecia", Font.BOLD, 22);
	private static final String IMG_END_TURN = "sysimages/endTurn.png";

	public ControlPanel(final Client client) {
		this.client = client;
		client.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (buttonNextTurn.isEnabled()) {
						selectNoAction();
					}
				}
			}
		});
		setLayout(new MigLayout("", "[]", "[][][]"));

			JPanel top = new JPanel(new MigLayout());
			nextTileLabel = new NextSquare(client);
			top.add(nextTileLabel, "");

			packSize = new JLabelWithAntialiasing("");
			packSize.setFont(cardFont);
			top.add(packSize, "wrap, gapleft 10");

			buttonNextTurn = new JButton(new ImageIcon(ControlPanel.class.getClassLoader().getResource(IMG_END_TURN)));
			buttonNextTurn.setToolTipText(_("Next"));
			buttonNextTurn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectNoAction();
				}
			});
			buttonNextTurn.setEnabled(false);
			top.add(buttonNextTurn, "wrap");

		add(top, "wrap");

		actionPanel = new ActionPanel(client);
		add(actionPanel, "wrap");

		playersPanel = new PlayersPanel(client);
		add(playersPanel, "wrap, grow");
	}

	public void selectNoAction() {
		if (getTileId() == null) {
			client.getServer().placeNoFigure();
		} else {
			client.getServer().placeNoTile();
		}
	}

	public ActionPanel getActionPanel() {
		return actionPanel;
	}

	public String getTileId() {
		if (nextTileLabel.getTile() == null) return null;
		return nextTileLabel.getTile().getId();
	}

	public Rotation getRotation() {
		if (nextTileLabel.getTile() == null) return null;
		return nextTileLabel.getTile().getRotation();
	}

	public void rotateTile() {
		if (nextTileLabel.getTile() != null && client.isClientActive()) {
			nextTileLabel.getTile().setRotation(getRotation().next());
			nextTileLabel.repaint();
			client.getGridPanel().repaint();
		}
	}

	public void started() {
		playersPanel.started();
	}

	public void tileDrawn(Tile tile) {
		nextTileLabel.setTile(tile);
		packSize.setText(client.getGame().getTilePack().tolalSize() + "");
	}

	public void selectTilePlacement(Set<Position> positions) {
		nextTileLabel.setEnabled(true);
		nextTileLabel.requestFocus();
	}

	public void selectAbbeyPlacement(Set<Position> positions) {
		nextTileLabel.setEnabled(true);
		nextTileLabel.requestFocus();
		buttonNextTurn.setEnabled(true);
	}

	public void tilePlaced(Tile tile) {
		nextTileLabel.setEmptyIcon();
	}

	public void selectAction(List<PlayerAction> actions, boolean canPass) {
		//direct collection sort can be unsupported - so copy to array first!
		PlayerAction[] arr = actions.toArray(new PlayerAction[actions.size()]);
		Arrays.sort(arr);
		actionPanel.setActions(arr);
		buttonNextTurn.setEnabled(canPass);
		buttonNextTurn.requestFocus();
		playersPanel.repaint();
	}

	public void clearActions() {
		actionPanel.clearActions();
		buttonNextTurn.setEnabled(false);

	}

	public void playerActivated(Player turn, Player active) {
		playersPanel.repaint();
	}

	public void closeGame() {
		clearActions();
		nextTileLabel.setEmptyIcon();
		buttonNextTurn.setEnabled(false);
		packSize.setText("");
	}

	public PlayersPanel getPlayersPanel() {
		return playersPanel;
	}

	public NextSquare getNextTileLabel() {
		return nextTileLabel;
	}
}

