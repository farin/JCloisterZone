package com.jcloisterzone.ui.controls;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.view.GameView;

import static com.jcloisterzone.ui.I18nUtils._;
import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;

public class ActionPanel extends MouseTrackingComponent implements ForwardBackwardListener, RegionMouseListener {

    public static final int FAKE_ACTION_SIZE = 62;
    public static final int LINE_HEIGHT = 30;
    public static final int LINE_Y = 46;
    public static final int PADDING = 3;
    public static final int LEFT_MARGIN = 10;

    public static final double ACTIVE_SIZE_RATIO = 1.375;

    private boolean active;
    private PlayerAction<?>[] actions;
    private int selectedActionIndex = -1;
    private boolean showConfirmRequest;

    //it has one flaw -  if game was just loaded, undo is not possible - may check gameView.getGame().isUndoAlloerd() - and update label
    private static final String CONFIRMATION_HINT = _("Confirm or undo a meeple placement.");
    private static final String REMOTE_CONFIRMATION_HINT = _("Waiting for a confirmation by remote player.");
    private static final String NO_ACTION_HINT = _("No action available. Pass or undo a tile placement.");

    private MultiLineLabel hintMessage;

    //cached scaled smooth images
    private Image[] selected, deselected;
    private int imgOffset = 0;
    private boolean refreshImages, refreshMouseRegions;

    private String fakeAction;
    private Image fakeActionImage;

    private final Client client;
    private final GameView gameView;

    public ActionPanel(GameView gameView) {
        this.client = gameView.getClient();
        this.gameView = gameView;

        Font hintFont = new Font(null, Font.ITALIC, 12);
        setLayout(new MigLayout());
        hintMessage = new MultiLineLabel();
        hintMessage.setFont(hintFont);
        hintMessage.setVisible(false);
        add(hintMessage, "pos 0 50 200 100");
        setOpaque(false);
    }


    public PlayerAction<?>[] getActions() {
        return actions;
    }

    public void setActions(boolean active, PlayerAction<?>[] actions) {
        this.active = active;
        selected = new Image[actions.length];
        deselected = new Image[actions.length];
        refreshImages = true;
        refreshMouseRegions = true;
        this.actions = actions;
        if (active) {
            if (actions.length > 0) {
                setSelectedActionIndex(0);
            } else {
            	hintMessage.setText(NO_ACTION_HINT);
                hintMessage.setVisible(true);
            }
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

        int availableWidth = getWidth() - LEFT_MARGIN - (actions.length-1)*PADDING;
        double units = actions.length + (ACTIVE_SIZE_RATIO-1.0);
        int baseSize = Math.min(maxIconSize, (int) Math.floor(availableWidth / units));
        int activeSize = (int) (baseSize * ACTIVE_SIZE_RATIO);

        Player activePlayer = gameView.getGame().getActivePlayer();
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
        hintMessage.setVisible(false);
        this.actions = null;
        this.selectedActionIndex = -1;
        refreshImages = true;
        refreshMouseRegions = true;
        fakeAction = null;
        active = false;
        repaint();
    }

    @Override
    public void forward() {
        if (active && selectedActionIndex != -1) {
            if (getSelectedAction() instanceof ForwardBackwardListener) {
                ((ForwardBackwardListener) getSelectedAction()).forward();
            } else {
                rollAction(1);
            }

        }
    }

    @Override
    public void backward() {
        if (active && selectedActionIndex != -1) {
            if (getSelectedAction() instanceof ForwardBackwardListener) {
                ((ForwardBackwardListener) getSelectedAction()).backward();
            } else {
                rollAction(-1);
            }
        }
    }

    public void rollAction(int change) {
        if (active) {
            if (actions.length == 0) return;
            int idx = (selectedActionIndex + change + actions.length) % actions.length;
            setSelectedActionIndex(idx);
            repaint();
        }
    }

    private void deselectAction() {
        if (this.selectedActionIndex != -1) {
            PlayerAction<?> prev = actions[this.selectedActionIndex];
            prev.deselect();
        }
    }

    private void setSelectedActionIndex(int selectedActionIndex) {
        deselectAction();
        this.selectedActionIndex = selectedActionIndex;
        PlayerAction<?> action = actions[selectedActionIndex];
        action.select(active);
    }

    public PlayerAction<?> getSelectedAction() {
        return selectedActionIndex == -1 ? null : actions[selectedActionIndex];
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (showConfirmRequest || hintMessage.isVisible()) {
            super.paint(g2);
            return;
        }

        g2.setColor(ControlPanel.PLAYER_BG_COLOR);
        g2.fillRoundRect(0, LINE_Y, getWidth()+CORNER_DIAMETER, LINE_HEIGHT, CORNER_DIAMETER, CORNER_DIAMETER);

        int x = LEFT_MARGIN;
        if (fakeActionImage != null) {
            g2.drawImage(fakeActionImage, x, LINE_Y+((LINE_HEIGHT-FAKE_ACTION_SIZE) / 2)+imgOffset, FAKE_ACTION_SIZE, FAKE_ACTION_SIZE, null);
        }

        if (actions != null && actions.length > 0) {
            //possible race condition - (but AtomBoolean cannot be used, too slow for painting)
            boolean refreshImages = this.refreshImages;
            this.refreshImages = false;
            boolean refreshMouseRegions = this.refreshMouseRegions;
            this.refreshMouseRegions = false;

            if (refreshImages) doRefreshImageCache();
            if (refreshMouseRegions) getMouseRegions().clear();

            for (int i = 0; i < actions.length; i++) {
                boolean active = (i == selectedActionIndex);

                Image img = active ? selected[i] : deselected[i];
                int size = img.getWidth(null);
                int iy = LINE_Y + (LINE_HEIGHT-size) / 2;

                if (refreshMouseRegions && selectedActionIndex != -1) {
                    getMouseRegions().add(new MouseListeningRegion(new Rectangle(x, iy+imgOffset, size, size), this, i));
                }
                g2.drawImage(img, x, iy+imgOffset, size, size, null);
                x += size + PADDING;
            }
        }
        super.paint(g2);
    }

    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        if (showConfirmRequest || actions == null || actions.length == 0) return;
        if (e.getButton() == MouseEvent.BUTTON1) {
            Integer i = (Integer) origin.getData();
            if (selectedActionIndex == i) {
                if (getSelectedAction() instanceof ForwardBackwardListener) {
                    if ((e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                        ((ForwardBackwardListener)getSelectedAction()).backward();
                    } else {
                        ((ForwardBackwardListener)getSelectedAction()).forward();
                    }
                }
            } else {
                setSelectedActionIndex(i);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
        if (showConfirmRequest) return;
        Integer i = (Integer) origin.getData();
        if (i != selectedActionIndex) {
            gameView.getGridPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseExited(MouseEvent e, MouseListeningRegion origin) {
        if (showConfirmRequest) return;
        gameView.getGridPanel().setCursor(Cursor.getDefaultCursor());
    }

    public String getFakeAction() {
        return fakeAction;
    }

    public void setFakeAction(String fakeAction) {
        this.fakeAction = fakeAction;
        if (fakeAction == null) {
            fakeActionImage = null;
        } else {
        	fakeActionImage = client.getResourceManager().getLayeredImage(new LayeredImageDescriptor("actions/"+fakeAction));
        	fakeActionImage = fakeActionImage.getScaledInstance(FAKE_ACTION_SIZE, FAKE_ACTION_SIZE, Image.SCALE_SMOOTH);
        }
        repaint();
    }

    public void setShowConfirmRequest(boolean showConfirmRequest, boolean remote) {
        this.showConfirmRequest = showConfirmRequest;
        if (showConfirmRequest) {
        	hintMessage.setText(remote ? REMOTE_CONFIRMATION_HINT : CONFIRMATION_HINT);
        }
        hintMessage.setVisible(showConfirmRequest);
    }
}
