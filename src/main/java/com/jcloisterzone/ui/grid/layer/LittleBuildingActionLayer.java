package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.action.LittleBuildingAction;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.theme.FigureTheme;

public class LittleBuildingActionLayer extends AbstractTileLayer implements ActionLayer<LittleBuildingAction> {

    private Map<LittleBuilding, Image> images = new HashMap<>();
    private LittleBuildingAction action;

    public LittleBuildingActionLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        FigureTheme theme = getClient().getFigureTheme();
        for (LittleBuilding lb : LittleBuilding.values()) {
            //System.err.println("lb-"+lb.name().toLowerCase());
            Image img = theme.getNeutralImage("lb-"+lb.name().toLowerCase());
            images.put(lb, img);
        }
    }

    @Override
    public void setAction(boolean active, LittleBuildingAction action) {
        this.action = action;
        setPosition(action == null ? null : getGame().getCurrentTile().getPosition());
    }

    @Override
    public LittleBuildingAction getAction() {
        return action;
    }

    @Override
    public void paint(Graphics2D g2) {
        // TODO Auto-generated method stub
        int icoSize = getSquareSize() / 2;
        int x = getOffsetX(), y =  getOffsetY();
        x -= icoSize / 2;
        y -= icoSize / 2;
        for (LittleBuilding lb : action.getOptions()) {
            g2.drawImage(images.get(lb), x, y, icoSize, icoSize, null);
            x += icoSize;
            x += icoSize / 10;
        }

    }
}
