package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.PANEL_WIDTH;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.MouseListeningRegion.RegionMouseListener;

public class ActionPanel extends FakeComponent implements RegionMouseListener {

    public static final int LINE_HEIGHT = 30;
    public static final int PADDING = 3;
    public static final int ICON_SIZE = 40;
    public static final int ACTIVE_ICON_SIZE = 50;

    private final Client client;
    private PlayerAction[] actions;
    private int selectedActionIndex = -1;

    //cached scaled smooth images
    private Image[] selected, deselected;
    private boolean refreshImages;

    public ActionPanel(Client client) {
        this.client = client;

        //TODO
//		addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (e.getButton() != MouseEvent.BUTTON1) return;
//				if (e.getX() < 2) return;
//				int index = (e.getX() - 2) / ICON_SIZE;
//				if (index < actions.length && index >= 0) {
//					setSelectedActionIndex(index);
//				}
//			}
//		});
    }

    private void repaint() {
        client.getMainPanel().repaint();
    }

    public PlayerAction[] getActions() {
        return actions;
    }

    public void setActions(PlayerAction[] actions) {
        if (client.isClientActive()) {
            selected = new Image[actions.length];
            deselected = new Image[actions.length];
            refreshImageCache();
            this.actions = actions;
            if (actions.length > 0) {
                setSelectedActionIndex(0);
            }
            repaint();
        }
    }


    public void refreshImageCache() {
        //refresh is executed in AWT thread
        refreshImages = true;
    }

    private void doRefreshImageCache() {
        if (actions == null) return;
        Player activePlayer = client.getGame().getActivePlayer();
        for(int i = 0; i < actions.length; i++) {
            selected[i] = new ImageIcon(
                actions[i].getImage(activePlayer, true).getScaledInstance(ACTIVE_ICON_SIZE, ACTIVE_ICON_SIZE, Image.SCALE_SMOOTH)
            ).getImage();
            deselected[i] = new ImageIcon(
                actions[i].getImage(activePlayer, false).getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH)
            ).getImage();
        }
    }

    public void clearActions() {
        deselectAction();
        refreshImages = false;
        selected = null;
        deselected = null;
        this.actions = null;
        this.selectedActionIndex = -1;
        repaint();
    }

    public void switchAction() {
        if (selectedActionIndex != -1) {
            getSelectedAction().switchAction();
        }
    }

    public void nextAction() {
        if (client.isClientActive()) {
            if (actions.length == 0) return;
            setSelectedActionIndex(selectedActionIndex == actions.length - 1 ? 0 : selectedActionIndex + 1);
            repaint();
            client.getGridPanel().repaint();
        }
    }

    private void deselectAction() {
        if (this.selectedActionIndex != -1) {
            PlayerAction prev = actions[this.selectedActionIndex];
            prev.deselect();
        }
    }

    private void setSelectedActionIndex(int selectedActionIndex) {
        deselectAction();
        this.selectedActionIndex = selectedActionIndex;
        PlayerAction action = actions[selectedActionIndex];
        action.select();
    }

    public PlayerAction getSelectedAction() {
        return actions[selectedActionIndex];
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);

        g2.setColor(ControlPanel.BG_COLOR);
        g2.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, LINE_HEIGHT, CORNER_DIAMETER, CORNER_DIAMETER);

        if (actions == null || actions.length == 0) return;

        if (refreshImages) {
            doRefreshImageCache();
            refreshImages = false;
        }

        int x = 2*PADDING;

        getMouseRegions().clear();
        for(int i = 0; i < actions.length; i++) {
            boolean active = (i == selectedActionIndex);

            Image img = active ? selected[i] : deselected[i];
            int size = img.getWidth(null);
            int iy = (LINE_HEIGHT-size) / 2;

            //TODO clean and create regions only on selected action change !!!
            getMouseRegions().add(new MouseListeningRegion(new Rectangle(x, iy, size, size), this, i));
            g2.drawImage(img, x, iy, size, size, null);
            x += size + PADDING;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        Integer i = (Integer) origin.getData();
        setSelectedActionIndex(i);
    }
}
