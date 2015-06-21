package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.I18nUtils._;

import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.wsio.message.DeployFlierMessage;


public class FeatureAreaLayer extends AbstractAreaLayer implements ActionLayer<SelectFeatureAction> {

    private SelectFeatureAction action;
    private boolean abbotOption = false;
    private boolean abbotOnlyOption = false;

    public FeatureAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setAction(boolean active, SelectFeatureAction action) {
        this.action = action;
    }

    @Override
    public SelectFeatureAction getAction() {
        return action;
    }

    protected Map<Location, FeatureArea> prepareAreas(Tile tile, Position p) {
        abbotOption = false;
        abbotOnlyOption = false;
        Set<Location> locations = action.getLocations(p);
        if (locations == null) return null;
        if (locations.contains(Location.ABBOT)) {
            abbotOption = true;
            if (!locations.contains(Location.CLOISTER)) {
                locations.add(Location.CLOISTER);
                abbotOnlyOption = true;
            }
            locations.remove(Location.ABBOT);
        }

        ConvenientResourceManager resMgr = getClient().getResourceManager();
        if (action instanceof BridgeAction) {
            return resMgr.getBridgeAreas(tile, getSquareSize(), locations);
        } else {
            return resMgr.getFeatureAreas(tile, getSquareSize(), locations);
        }
    }


    @Override
    protected void performAction(final Position pos, Location loc) {
        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;

            if (loc == Location.FLIER) {
                getClient().getConnection().send(new DeployFlierMessage(getGame().getGameId(), ma.getMeepleType()));
                return;
            }
            if (loc == Location.CLOISTER && abbotOption) {
                String[] options;
                if (abbotOnlyOption) {
                    options = new String[] {_("Place as abbot")};
                } else {
                    options = new String[] {_("Place as monk"), _("Place as abbot") };
                }
                int result = JOptionPane.showOptionDialog(getClient(),
                    _("How do you want to place follower on monastery?"),
                    _("Monastery"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (result == -1) { //closed dialog
                    return;
                }
                if (abbotOnlyOption || result == JOptionPane.NO_OPTION) {
                    loc = Location.ABBOT;
                }
            }
        }
        action.perform(getRmiProxy(), new FeaturePointer(pos, loc));
        return;
    }


}
