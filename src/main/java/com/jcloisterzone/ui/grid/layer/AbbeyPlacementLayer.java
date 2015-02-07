package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;

public class AbbeyPlacementLayer extends AbstractTilePlacementLayer implements ActionLayer<AbbeyPlacementAction> {

    private AbbeyPlacementAction action;

    public AbbeyPlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setAction(boolean active, AbbeyPlacementAction action) {
        this.action = action;
        setActive(active);
        setAvailablePositions(action == null ? null : action.getOptions());
    }

    @Override
    public AbbeyPlacementAction getAction() {
        return action;
    }

    @Override
    protected Image createPreviewIcon() {
        return getClient().getResourceManager().getAbbeyImage();
    }

    @Override
    protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position previewPosition) {
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(ALLOWED_PREVIEW);
        g2.drawImage(previewIcon, getAffineTransform(previewIcon.getWidth(null), previewPosition), null);
        g2.setComposite(compositeBackup);
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && isActive()) {
                e.consume();
                action.perform(getRmiProxy(), p);
            }
        }
    }

}
