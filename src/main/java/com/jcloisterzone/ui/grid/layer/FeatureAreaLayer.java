package com.jcloisterzone.ui.grid.layer;

import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.wsio.message.DeployFlierMessage;

import static com.jcloisterzone.ui.I18nUtils._;


public class FeatureAreaLayer extends AbstractAreaLayer implements ActionLayer<SelectFeatureAction> {

    private SelectFeatureAction action;
    private boolean abbotOption = false;

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

    protected Map<Location, Area> prepareAreas(Tile tile, Position p) {
        abbotOption = false;
        Set<Location> locations = action.getLocations(p);
        if (locations == null) return null;
        if (locations.contains(Location.ABBOT)) {
            abbotOption = true;
            locations.remove(Location.ABBOT);
        }
        if (action instanceof BridgeAction) {
            return getClient().getResourceManager().getBridgeAreas(tile, getSquareSize(), locations);
        } else {
            return getClient().getResourceManager().getFeatureAreas(tile, getSquareSize(), locations);
        }
    }


//    private boolean confirmFarmPlacement() {
//        String options[] = {_("Place a follower"), _("Cancel") };
//        int result = JOptionPane.showOptionDialog(getClient(),
//                _("Do you really want to place a follower on farm?"),
//                _("Confirm follower placement"),
//                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//        return JOptionPane.YES_OPTION == result;
//    }
//
//    private boolean confirmTowerPlacement(Position pos) {
//        int result;
//        Player activePlayer = getGame().getActivePlayer();
//        if (getGame().getCapability(TowerCapability.class).getTowerPieces(activePlayer) > 0) {
//            String options[] = {
//                _("Confirm follower placement"),
//                _("Cancel"),
//                _("Place a tower piece")
//            };
//            result = JOptionPane.showOptionDialog(getClient(),
//                _("Do you really want to place a follower on the tower?\n(To prevent tower from adding more pieces on the top)"),
//                _("Confirm follower placement"),
//                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//        } else {
//            String options[] = {
//                _("Confirm follower placement"),
//                _("Cancel")
//            };
//            result = JOptionPane.showOptionDialog(getClient(),
//                _("Do you really want to place a follower on the tower?\n(To prevent tower from adding more pieces on the top)"),
//                _("Confirm follower placement"),
//                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//        }
//        if (result == JOptionPane.CANCEL_OPTION) {
//            //place tower piece instead
//            for (PlayerAction<?> action : getClient().getControlPanel().getActionPanel().getActions()) {
//                if (action instanceof TowerPieceAction) {
//                    ((TowerPieceAction) action).perform(getRmiProxy(), pos);
//                    gridPanel.hideLayer(this);
//                    return false;
//                }
//            }
//            return false;
//        }
//        return result == JOptionPane.YES_OPTION;
//    }


    @Override
    protected void performAction(final Position pos, Location loc) {
        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;
            Feature piece = gridPanel.getTile(pos).getFeature(loc);
//            if (piece instanceof Farm) {
//                if (Follower.class.isAssignableFrom(ma.getMeepleType()) && getClient().getConfig().getConfirm().getFarm_place()) {
//                    if (!confirmFarmPlacement()) return;
//                }
//            } else if (piece instanceof Tower) {
//                if (getClient().getConfig().getConfirm().getTower_place()) {
//                    if (!confirmTowerPlacement(pos)) return;
//                }
//            }
            if (loc == Location.FLIER) {
                getClient().getConnection().send(new DeployFlierMessage(getGame().getGameId(), ma.getMeepleType()));
                return;
            }
            if (loc == Location.CLOISTER && abbotOption) {
                String options[] = {_("Place as monk"), _("Place as abbot") };
                int result = JOptionPane.showOptionDialog(getClient(),
                    _("How do you want to place follower on monastery?"),
                    _("Monastery"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (result == -1) { //closed dialog
                    return;
                }
                if (result == JOptionPane.NO_OPTION) {
                    loc = Location.ABBOT;
                }
            }
        }
        action.perform(getRmiProxy(), new FeaturePointer(pos, loc));
        return;
    }


}
