package com.jcloisterzone.ui.grid.layer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.controls.action.TilePlacementActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.TileImage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class TilePlacementLayer extends AbstractGridLayer implements ActionLayer, GridMouseListener, ForwardBackwardListener {

    private static final Composite ALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
    private static final Composite DISALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
    private static final Composite BRIDGE_PREVIEW_FILL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

    private boolean rotateWihtMouse = false;
    private boolean active;
    private Set<Position> availablePositions;
    private Position previewPosition;

    private TilePlacementActionWrapper actionWrapper;

    private Set<PlacementOption> allowedRotations;
    private boolean allowedRotation;
    private Rotation realRotation;
    private Rotation previewRotation;
    private FeaturePointer previewBridge;


    public TilePlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setActionWrapper(boolean active, ActionWrapper actionWrapper) {
        this.actionWrapper = (TilePlacementActionWrapper) actionWrapper;
        setActive(active);
        if (actionWrapper == null) {
            availablePositions = null;
            realRotation = null;
        } else {
            this.actionWrapper.setForwardBackwardDelegate(this);
            availablePositions = getAction().getOptions().map(tp -> tp.getPosition()).distinct();
        };
    }

    @Override
    public TilePlacementActionWrapper getActionWrapper() {
        return actionWrapper;
    }

    @Override
    public TilePlacementAction getAction() {
        return (TilePlacementAction) getActionWrapper().getAction();
    }

    @Override
    public void onShow() {
        super.onShow();
        attachMouseInputListener(new GridMouseAdapter(gridPanel, this));
    }

    @Override
    public void onHide() {
        super.onHide();
        availablePositions = null;
        previewPosition = null;
    }

    private void drawPreviewIcon(Graphics2D g2, Position previewPosition) {
        if (realRotation != actionWrapper.getTileRotation()) {
            preparePreviewRotation(previewPosition);
        }
        TileImage previewIcon = rm.getTileImage(getAction().getTile().getId(), previewRotation);
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(allowedRotation ? ALLOWED_PREVIEW : DISALLOWED_PREVIEW);
        g2.drawImage(previewIcon.getImage(), getAffineTransform(previewIcon, previewPosition), null);
        g2.setComposite(compositeBackup);
    }

    // Draws circles for all valid rotations that show the user where to move their
    // mouse to.
    private void drawRotationHandle(Graphics2D g2, Position previewPosition) {
        Set<Rotation> allowed = allowedRotations.map(PlacementOption::getRotation);
        List.of(Rotation.values())
                .filter(r -> allowed.contains(realRotation.add(r)))
                .forEach(rotation -> {
                    Point2D p = getRotationHandlePositions(previewPosition, rotation);
                    g2.setColor(new Color(0f, 0f, 0f, 0.5f));
                    if (realRotation.add(rotation).equals(previewRotation)) {
                        // Draw a filled oval for the current one...
                        g2.fillOval(
                                (int) p.getX() - getTileWidth() / 20,
                                (int) p.getY() - getTileHeight() / 20,
                                getTileWidth() / 9,
                                getTileHeight() / 9
                        );
                    } else {
                        // ...otherwise just draw the outlines
                        g2.setStroke(new BasicStroke((int)Math.ceil(getTileWidth() / 75.0)));
                        g2.drawOval(
                                (int) p.getX() - getTileWidth() / 20,
                                (int) p.getY() - getTileHeight() / 20,
                                getTileWidth() / 9,
                                getTileHeight() / 9
                        );
                    }
        });

    }

    private Point2D getRotationHandlePositions(Position p, Rotation r) {
        int w = getTileWidth(),
            h = getTileHeight();
        switch (r) {
            case R0: return new Point(p.x * w + w / 2, p.y * h + h * 5 / 6);
            case R180: return new Point(p.x * w + w / 2, p.y * h + h / 6);
            case R90: return new Point(p.x * w + w / 6, p.y * h + h / 2);
            case R270: return new Point(p.x * w + w * 5 / 6, p.y * h + h / 2);
        }
        throw new NullPointerException("Rotation can't be null");
    }

    private double getSquareDistanceToRotationHandle(Position p, Rotation r, Point2D point) {
        Point2D handle = getRotationHandlePositions(p, r);
        return Math.pow(handle.getX() - point.getX(), 2) + Math.pow(handle.getY() - point.getY(), 2);
    }

    private void preparePreviewRotation(Position p) {
        realRotation = getActionWrapper().getTileRotation();
        previewRotation = realRotation;

        allowedRotations = getAction().getOptions().filter(tp -> tp.getPosition().equals(p));
        PlacementOption matchingPlacement = allowedRotations
            .find(tp -> tp.getRotation().equals(previewRotation))
            .getOrNull();

        if (matchingPlacement != null) {
            allowedRotation = true;
            previewBridge = matchingPlacement.getMandatoryBridge();
        } else {
            TileSymmetry symmetry = getAction().getTile().getSymmetry();
            if (allowedRotations.size() == 1 || symmetry == TileSymmetry.S2) {
                PlacementOption tp = allowedRotations.get();
                allowedRotation = true;
                previewRotation = tp.getRotation();
                previewBridge = tp.getMandatoryBridge();
            } else {
                allowedRotation = false;
                previewBridge = null;
            }
        }
    }

    @Override
    public void forward() {
        rotate(Rotation.R90);
    }

    @Override
    public void backward() {
        rotate(Rotation.R270);
    }

    private void rotate(Rotation spin) {
        Rotation current = getActionWrapper().getTileRotation();
        Rotation next = current.add(spin);
        if (getPreviewPosition() != null) {
            Set<Rotation> rotations = getAction().getRotations(getPreviewPosition());
            if (!rotations.isEmpty()) {
                if (rotations.size() == 1) {
                    next = rotations.iterator().next();
                } else {
                    if (rotations.contains(current)) {
                        while (!rotations.contains(next)) next = next.add(spin);
                    } else {
                        if (getAction().getTile().getSymmetry() == TileSymmetry.S2 && rotations.size() == 2) {
                            //if S2 and size == 2 rotate to flip preview to second choice
                            next = next.add(spin);
                        } else {
                            next = current;
                        }
                        while (!rotations.contains(next)) next = next.add(spin);
                    }
                }
            }
        }
        getActionWrapper().setTileRotation(next);
        ActionPanel panel = gc.getGameView().getControlPanel().getActionPanel();
        panel.refreshImageCache();
        gridPanel.repaint();
    }

    @Override
    public void paint(Graphics2D g2) {
        if (availablePositions == null) return;
        int xSize = getTileWidth() - 4,
            ySize = getTileHeight() - 4,
            shift = 2,
            thickness = xSize/14;

        g2.setColor(getClient().getTheme().getTilePlacementColor());
        for (Position p : availablePositions) {
            if (previewPosition == null || !previewPosition.equals(p)) {
                int x = getOffsetX(p)+shift, y = getOffsetY(p)+shift;
                g2.fillRect(x, y, xSize, thickness);
                g2.fillRect(x, y+ySize-thickness, xSize, thickness);
                g2.fillRect(x, y, thickness, ySize);
                g2.fillRect(x+xSize-thickness, y, thickness, ySize);
            }
        }

        if (previewPosition != null) {
            drawPreviewIcon(g2, previewPosition);
            if (rotateWihtMouse) {
                drawRotationHandle(g2, previewPosition);
            }
        }
    }

    public void paintBridgePreview(Graphics2D g2) {
        if (previewBridge != null) {
            Composite oldComposite = g2.getComposite();
            g2.setColor(Color.WHITE);
            g2.setComposite(BRIDGE_PREVIEW_FILL_COMPOSITE);

            Position bridgePos = previewBridge.getPosition();
            Location bridgeLoc = previewBridge.getLocation();
            FeatureArea fa = rm.getBridgeArea(bridgeLoc).translateTo(bridgePos);
            Area a = fa.getDisplayArea();
            g2.fill(a.createTransformedArea(getZoomScale()));
            g2.setComposite(oldComposite);
        }
    }

    @Override
    public void tileEntered(MouseEvent e, Position p) {
        rotateWihtMouse = getConfig().getTile_rotation() == Config.TileRotationControls.TAB_RCLICK_MOUSEMOVE; // get current value from config
        realRotation = null;
        if (availablePositions.contains(p)) {
            previewPosition = p;
            gridPanel.repaint();
        }
    }

    @Override
    public void tileExited(MouseEvent e, Position p) {
        realRotation = null;
        if (previewPosition != null) {
            previewPosition = null;
            previewBridge = null;
            gridPanel.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && isActive() && allowedRotation) {
                e.consume();
                gc.getConnection().send(getAction().select(new PlacementOption(p, previewRotation, null)));
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e, Position p) {
        if (rotateWihtMouse && realRotation != null) {
            Point2D point = gridPanel.getRelativePoint(e.getPoint());
            Tuple2<Rotation, Double> closest = List.of(Rotation.values())
                    .map(r -> new Tuple2<>(r, getSquareDistanceToRotationHandle(p, r, point)))
                    .minBy(Tuple2::_2)
                    .getOrNull();

            if (closest != null) {
                Rotation rot = closest._1;
                double distance = closest._2;

                if (distance <= getTileWidth() * getTileHeight() / 40) {
                    Rotation combinedRot = realRotation.add(rot);
                    if (previewRotation != combinedRot) {
                        PlacementOption matchingPlacement = allowedRotations.filter(opt -> opt.getRotation() == combinedRot).getOrNull();
                        if (matchingPlacement != null) {
                            previewRotation = combinedRot;
                            previewBridge = matchingPlacement.getMandatoryBridge();
                            allowedRotation = true;
                            gridPanel.repaint();
                        }
                    }
                }
            }
        }
    }

    public Position getPreviewPosition() {
        return previewPosition;
    };

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
