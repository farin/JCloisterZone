package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.theme.FigureTheme;

public class LittleBuildingActionLayer extends AbstractTileLayer implements ActionLayer<LittleBuildingAction>, GridMouseListener {

	protected static final Composite SHADOW_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
	private static final double PADDING_RATIO = 0.10;

    private Map<LittleBuilding, Image> images = new HashMap<>();
    private LittleBuildingAction action;
    private LittleBuilding selected = null;

    private HashMap<LittleBuilding, Rectangle> areas = new HashMap<>();

    public LittleBuildingActionLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        FigureTheme theme = getClient().getFigureTheme();
        for (LittleBuilding lb : LittleBuilding.values()) {
            Image img = theme.getNeutralImage("lb-"+lb.name().toLowerCase());
            images.put(lb, img);
        }
    }

    @Override
    public void setAction(boolean active, LittleBuildingAction action) {
        this.action = action;
        setPosition(action == null ? null : getGame().getCurrentTile().getPosition());
        if (active) {
        	prepareAreas();
        } else {
        	selected = null;
        	areas.clear();
        }
    }

    @Override
    public LittleBuildingAction getAction() {
        return action;
    }

    @Override
    public void zoomChanged(int squareSize) {
    	areas.clear();
    	prepareAreas();
    }

    private void prepareAreas() {
    	if (action == null) return;

	    int icoSize = getSquareSize() / 2;
	    int padding = (int) (icoSize * PADDING_RATIO);
        int x = getOffsetX(), y =  getOffsetY();
        x -= icoSize / 2 + padding * 3;
        y -= icoSize / 2;

        for (LittleBuilding lb : LittleBuilding.values()) {
        	if (action.getOptions().contains(lb)) {
        		Rectangle r = new Rectangle(x-padding, y-padding, icoSize+2*padding, icoSize+2*padding);
        		areas.put(lb, r);
        	}
        	x += icoSize;
            x += 3*padding;
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        // TODO board rotation!!!
        int icoSize = getSquareSize() / 2;
        int padding = (int) (icoSize * PADDING_RATIO);
        Composite origComposite = g2.getComposite();
        for (LittleBuilding lb : action.getOptions()) {
        	Rectangle r = areas.get(lb);
        	if (r == null) continue;
        	g2.setComposite(SHADOW_COMPOSITE);
        	g2.setColor(Color.BLACK);
        	g2.fillRect(r.x+3, r.y+3, r.width, r.height);
        	g2.setComposite(origComposite);
        	g2.setColor(selected == lb ? Color.WHITE : Color.GRAY);
        	g2.fill(r);
            g2.drawImage(images.get(lb), r.x+padding, r.y+padding, icoSize, icoSize, null);
        }

    }

    private class MoveTrackingGridMouseAdapter extends GridMouseAdapter {

        public MoveTrackingGridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
            super(gridPanel, listener);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point2D point = gridPanel.getRelativePoint(e.getPoint());
            int x = (int) point.getX();
            int y = (int) point.getY();
            LittleBuilding newValue = null;
            for (Entry<LittleBuilding, Rectangle> entry : areas.entrySet()) {
                if (entry.getValue().contains(x, y)) {
                    newValue = entry.getKey();
                    break;
                }
            }
            if (newValue != selected) {
                selected = newValue;
                gridPanel.repaint();
            }
        }

    }

    @Override
    protected GridMouseAdapter createGridMouserAdapter(GridMouseListener listener) {
        return new MoveTrackingGridMouseAdapter(gridPanel, listener);
    }

	@Override
	public void squareEntered(MouseEvent e, Position p) {
	}

	@Override
	public void squareExited(MouseEvent e, Position p) {

	}

	@Override
	public void mouseClicked(MouseEvent e, Position p) {
		if (e.getButton() == MouseEvent.BUTTON1) {
            if (selected != null) {
            	action.perform(getRmiProxy(), selected);
                e.consume();
            }
        }
	}


}
