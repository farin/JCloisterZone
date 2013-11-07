package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.PANEL_WIDTH;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;

public class ActionPanel extends FakeComponent implements RegionMouseListener, ForwardBackwardListener {

    public static final int LINE_HEIGHT = 30;
    public static final int PADDING = 3;
    public static final int LEFT_MARGIN = 10;
    public static final double ACTIVE_SIZE_RATIO = 1.375;

    private PlayerAction[] actions;
    private int selectedActionIndex = -1;

    //cached scaled smooth images
    private Image[] selected, deselected;
    private int imgOffset = 0;
    private boolean refreshImages, refreshMouseRegions;

    public ActionPanel(Client client) {
        super(client);
    }

    private void repaint() {
        client.getGridPanel().repaint();
    }

    public PlayerAction[] getActions() {
        return actions;
    }

    public void setActions(PlayerAction[] actions) {
        selected = new Image[actions.length];
        deselected = new Image[actions.length];
        refreshImages = true;
        refreshMouseRegions = true;
        this.actions = actions;
        if (actions.length > 0 && client.isClientActive()) {
            setSelectedActionIndex(0);
        }
        repaint();
    }


    public void refreshImageCache() {
        //refresh is executed in AWT thread
        refreshImages = true;
        repaint();
    }

    private void doRefreshImageCache() {
        if (actions == null || actions.length == 0) {
            selected = null;
            deselected = null;
        }

        int maxIconSize = 40;
        imgOffset = 0;

        if (actions[0] instanceof TilePlacementAction) {
            imgOffset = -10;
            maxIconSize = 62;
        } else if (actions[0] instanceof AbbeyPlacementAction) {
            imgOffset = 4;
            maxIconSize = 52;
        }  else {
            maxIconSize = 40;
        }

        int availableWidth = ControlPanel.PANEL_WIDTH - LEFT_MARGIN - (actions.length-1)*PADDING;
        double units = actions.length + (ACTIVE_SIZE_RATIO-1.0);
        int baseSize = Math.min(maxIconSize, (int) Math.floor(availableWidth / units));
        int activeSize = (int) (baseSize * ACTIVE_SIZE_RATIO);

        Player activePlayer = client.getGame().getActivePlayer();
        for (int i = 0; i < actions.length; i++) {
            selected[i] = new ImageIcon(
                actions[i].getImage(activePlayer, true).getScaledInstance(activeSize, activeSize, Image.SCALE_SMOOTH)
            ).getImage();
            deselected[i] = new ImageIcon(
                actions[i].getImage(activePlayer, false).getScaledInstance(baseSize, baseSize, Image.SCALE_SMOOTH)
            ).getImage();
        }
    }

    public void clearActions() {
        deselectAction();
        this.actions = null;
        this.selectedActionIndex = -1;
        refreshImages = true;
        refreshMouseRegions = true;
        repaint();
    }

    public void forward() {
        if (selectedActionIndex != -1) getSelectedAction().forward();
    }

    public void backward() {
        if (selectedActionIndex != -1) getSelectedAction().backward();
    }

    public void rollAction(int change) {
        if (client.isClientActive()) {
            if (actions.length == 0) return;
            int idx = (selectedActionIndex + change + actions.length) % actions.length;
            setSelectedActionIndex(idx);
            repaint();
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

        g2.setColor(ControlPanel.PLAYER_BG_COLOR);
        g2.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, LINE_HEIGHT, CORNER_DIAMETER, CORNER_DIAMETER);

        if (actions == null || actions.length == 0) return;

        //possible race condition - (but AtomBoolean cannot be used, too slow for painting)
        boolean refreshImages = this.refreshImages;
        this.refreshImages = false;
        boolean refreshMouseRegions = this.refreshMouseRegions;
        this.refreshMouseRegions = false;

        if (refreshImages) {
            doRefreshImageCache();
        }

        int x = LEFT_MARGIN;

        if (refreshMouseRegions) {
            getMouseRegions().clear();
        }
        for (int i = 0; i < actions.length; i++) {
            boolean active = (i == selectedActionIndex);

            Image img = active ? selected[i] : deselected[i];
            int size = img.getWidth(null);
            int iy = (LINE_HEIGHT-size) / 2;

            if (refreshMouseRegions && selectedActionIndex != -1) {
                getMouseRegions().add(new MouseListeningRegion(new Rectangle(x, iy+imgOffset, size, size), this, i));
            }
            g2.drawImage(img, x, iy+imgOffset, size, size, null);
            x += size + PADDING;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Integer i = (Integer) origin.getData();
            setSelectedActionIndex(i);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
        Integer i = (Integer) origin.getData();
        if (i != selectedActionIndex) {
            client.getGridPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseExited(MouseEvent e, MouseListeningRegion origin) {
        client.getGridPanel().setCursor(Cursor.getDefaultCursor());
    }
}
