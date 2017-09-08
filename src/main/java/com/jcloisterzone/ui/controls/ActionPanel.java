package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._;
import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.NeutralFigureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.controls.action.MeepleActionWrapper;
import com.jcloisterzone.ui.controls.action.NeutralFigureActionWrapper;
import com.jcloisterzone.ui.controls.action.TilePlacementActionWrapper;
import com.jcloisterzone.ui.controls.action.TunnelActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.view.GameView;

import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import net.miginfocom.swing.MigLayout;

public class ActionPanel extends MouseTrackingComponent implements ForwardBackwardListener, RegionMouseListener {

    public static final int FAKE_ACTION_SIZE = 62;
    public static final int LINE_HEIGHT = 30;
    public static final int LINE_Y = 46;
    public static final int PADDING = 3;
    public static final int LEFT_MARGIN = 10;

    public static final double ACTIVE_SIZE_RATIO = 1.375;

    private ActionsState actionsState;
    private boolean active;
    private IndexedSeq<ActionWrapper> actions = Vector.empty();
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


    public IndexedSeq<ActionWrapper> getActions() {
        return actions;
    }

    public void onPlayerActionsChanged(ActionsState actionsState) {
        this.actionsState = actionsState;
        Vector<PlayerAction<?>> actions = actionsState.getActions();

        active = actionsState.getPlayer().isLocalHuman();
        selected = new Image[actions.size()];
        deselected = new Image[actions.size()];
        refreshImages = true;
        refreshMouseRegions = true;

        this.actions = actions.map(a -> {
            if (a instanceof TilePlacementAction) {
                return new TilePlacementActionWrapper((TilePlacementAction) a);
            }
            if (a instanceof MeepleAction) {
                return new MeepleActionWrapper((MeepleAction) a);
            }
            if (a instanceof TunnelAction) {
                return new TunnelActionWrapper((TunnelAction) a);
            }
            if (a instanceof NeutralFigureAction) {
                return new NeutralFigureActionWrapper((NeutralFigureAction) a);
            }
            return new ActionWrapper(a);
        });
        if (active) {
            if (actions.isEmpty()) {
                hintMessage.setText(NO_ACTION_HINT);
                hintMessage.setVisible(true);
            } else {
                setSelectedActionIndex(0);
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
        if (actions.isEmpty()) {
            selected = null;
            deselected = null;
            return;
        }

        int maxIconSize = 40;
        imgOffset = 0;

        PlayerAction<?> action = actions.get().getAction();

        if (action instanceof TilePlacementAction) {
            TilePlacementAction tpa = (TilePlacementAction) action;
            if (tpa.getTile().isAbbeyTile() || actionsState.isPassAllowed()) {
                imgOffset = 4;
                maxIconSize = 52;
            } else {
                imgOffset = -10;
                maxIconSize = 62;
            }
        }  else {
            maxIconSize = 40;
        }

        int availableWidth = getWidth() - LEFT_MARGIN - (actions.size() - 1)*PADDING;
        double units = actions.size()  + (ACTIVE_SIZE_RATIO-1.0);
        int baseSize = Math.min(maxIconSize, (int) Math.floor(availableWidth / units));
        int activeSize = (int) (baseSize * ACTIVE_SIZE_RATIO);

        Player activePlayer = gameView.getGame().getState().getActivePlayer();
        for (int i = 0; i < actions.size(); i++) {
            selected[i] = new ImageIcon(
                actions.get(i).getImage(client.getResourceManager(), activePlayer, true).getScaledInstance(activeSize, activeSize, Image.SCALE_SMOOTH)
            ).getImage();
            deselected[i] = new ImageIcon(
                actions.get(i).getImage(client.getResourceManager(), activePlayer, false).getScaledInstance(baseSize, baseSize, Image.SCALE_SMOOTH)
            ).getImage();
        }
    }

    public void clearActions() {
        deselectAction();
        hintMessage.setVisible(false);
        this.actions = Vector.empty();
        this.selectedActionIndex = -1;
        refreshImages = true;
        refreshMouseRegions = true;
        active = false;
        actionsState = null;
        repaint();
    }

    @Override
    public void forward() {
        if (active) {
            ActionWrapper selected = getSelectedActionWrapper().getOrNull();
            if (selected instanceof ForwardBackwardListener) {
                ((ForwardBackwardListener) selected).forward();
            } else {
                rollAction(1);
            }

        }
    }

    @Override
    public void backward() {
        if (active && selectedActionIndex != -1) {
            ActionWrapper selected = getSelectedActionWrapper().getOrNull();
            if (selected instanceof ForwardBackwardListener) {
                ((ForwardBackwardListener) selected).backward();
            } else {
                rollAction(-1);
            }
        }
    }

    public void rollAction(int change) {
        if (active && !actions.isEmpty()) {
            int idx = (selectedActionIndex + change + actions.size()) % actions.size();
            setSelectedActionIndex(idx);
            repaint();
        }
    }

    private void deselectAction() {
        ActionWrapper prev = getSelectedActionWrapper().getOrNull();
        if (prev == null) return;

        ActionLayer layer = getActionLayer(prev);
        if (layer != null) {
            layer.setActionWrapper(false, null);
            gameView.getGridPanel().hideLayer(layer);
        }
    }

    @SuppressWarnings("unchecked")
    private ActionLayer getActionLayer(ActionWrapper actionWrapper) {
        PlayerAction<?> action = actionWrapper.getAction();
        if (!action.getClass().isAnnotationPresent(LinkedGridLayer.class)) {
            return null;
        }
        Class<? extends ActionLayer> layerType = action.getClass().getAnnotation(LinkedGridLayer.class).value();
        return gameView.getGridPanel().findLayer(layerType);
    }

    private void setSelectedActionIndex(int selectedActionIndex) {
        deselectAction();
        this.selectedActionIndex = selectedActionIndex;
        ActionWrapper actionWrapper = getSelectedActionWrapper().getOrNull();

        if (actionWrapper == null) return;
        ActionLayer layer = getActionLayer(actionWrapper);
        if (layer != null) {
            layer.setActionWrapper(active, actionWrapper);
            gameView.getGridPanel().showLayer(layer);
        }
    }

    public Option<ActionWrapper> getSelectedActionWrapper() {
        if (selectedActionIndex == -1) return Option.none();
         return Option.some(actions.get(selectedActionIndex));
    }

    public Option<PlayerAction<?>> getSelectedAction() {
        return getSelectedActionWrapper().map(ActionWrapper::getAction);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (showConfirmRequest || hintMessage.isVisible()) {
            super.paint(g2);
            return;
        }

        g2.setColor(client.getTheme().getPlayerBoxBg());
        g2.fillRoundRect(0, LINE_Y, getWidth()+CORNER_DIAMETER, LINE_HEIGHT, CORNER_DIAMETER, CORNER_DIAMETER);

        int x = LEFT_MARGIN;

        if (!actions.isEmpty()) {
            //possible race condition - (but AtomBoolean cannot be used, too slow for painting)
            boolean refreshImages = this.refreshImages;
            this.refreshImages = false;
            boolean refreshMouseRegions = this.refreshMouseRegions;
            this.refreshMouseRegions = false;

            if (refreshImages) doRefreshImageCache();
            if (refreshMouseRegions) getMouseRegions().clear();

            for (int i = 0; i < actions.size(); i++) {
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
        if (showConfirmRequest || actions.isEmpty()) return;
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

    public void setShowConfirmRequest(boolean showConfirmRequest, boolean remote) {
        this.showConfirmRequest = showConfirmRequest;
        if (showConfirmRequest) {
            hintMessage.setText(remote ? REMOTE_CONFIRMATION_HINT : CONFIRMATION_HINT);
        }
        hintMessage.setVisible(showConfirmRequest);
    }
}
