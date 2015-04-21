package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.GridPanel;

public class TilePlacementLayer extends AbstractTilePlacementLayer implements ActionLayer<TilePlacementAction>, ForwardBackwardListener {

    private TilePlacementAction action;

    private Rotation realRotation;
    private Rotation previewRotation;
    private boolean allowedRotation;

    public TilePlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setAction(boolean active, TilePlacementAction action) {
        this.action = action;
        setActive(active);
        if (action == null) {
            setAvailablePositions(null);
            realRotation = null;
        } else {
        	action.setForwardBackwardDelegate(this);
            setAvailablePositions(action.groupByPosition().keySet());
        };
    }

    @Override
    public TilePlacementAction getAction() {
        return null;
    }

    @Override
    protected Image createPreviewIcon() {
        return getClient().getResourceManager().getTileImage(action.getTile());
    }

    @Override
    protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position previewPosition) {
        if (realRotation != action.getTileRotation()) {
            preparePreviewRotation(previewPosition);
        }
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(allowedRotation ? ALLOWED_PREVIEW : DISALLOWED_PREVIEW);
        g2.drawImage(previewIcon, getAffineTransform(previewIcon.getWidth(null), previewPosition, previewRotation), null);
        g2.setComposite(compositeBackup);
    }

    private void preparePreviewRotation(Position p) {
        realRotation = action.getTileRotation();
        previewRotation = realRotation;

        Set<Rotation> allowedRotations = action.getRotations(p);
        if (allowedRotations.contains(previewRotation)) {
            allowedRotation = true;
        } else {
            if (allowedRotations.size() == 1) {
                previewRotation = allowedRotations.iterator().next();
                allowedRotation = true;
            } else if (action.getTile().getSymmetry() == TileSymmetry.S2) {
                previewRotation = realRotation.next();
                allowedRotation = true;
            } else {
                allowedRotation = false;
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
    	Rotation current = action.getTileRotation();
    	Rotation next = current.add(spin);
    	if (getPreviewPosition() != null) {
    		Set<Rotation> rotations = action.getRotations(getPreviewPosition());
    		if (!rotations.isEmpty()) {
	    		if (rotations.size() == 1) {
	    			next = rotations.iterator().next();
	    		} else {

	    			if (rotations.contains(current)) {
	    				while (!rotations.contains(next)) next = next.add(spin);
	    			} else {
	    				if (action.getTile().getSymmetry() == TileSymmetry.S2 && rotations.size() == 2) {
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
    	action.setTileRotation(next);
        ActionPanel panel = action.getMainPanel().getControlPanel().getActionPanel();
        panel.refreshImageCache();
        action.getMainPanel().getGridPanel().repaint();
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        realRotation = null;
        super.squareEntered(e, p);
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
        realRotation = null;
        super.squareExited(e, p);
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && isActive() && allowedRotation) {
                e.consume();
                action.perform(getRmiProxy(), new TilePlacement(p, previewRotation));
            }
        }
    }

}
