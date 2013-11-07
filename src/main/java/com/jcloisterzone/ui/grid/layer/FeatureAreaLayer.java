package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.dialog.AmbiguousUndeployDialog;
import com.jcloisterzone.ui.dialog.AmbiguousUndeployDialog.AmbiguousUndeployDialogEvent;
import com.jcloisterzone.ui.grid.GridPanel;


public class FeatureAreaLayer extends AbstractAreaLayer {

    private final SelectFeatureAction action;

    public FeatureAreaLayer(GridPanel gridPanel, SelectFeatureAction action) {
        super(gridPanel);
        this.action = action;
    }

    protected Map<Location, Area> prepareAreas(Tile tile, Position p) {
        Set<Location> locations = action.getLocationsMap().get(p);
        if (locations == null) return null;
        if (action instanceof BridgeAction) {
            return getClient().getResourceManager().getBridgeAreas(tile, getSquareSize(), locations);
        } else {
            return getClient().getResourceManager().getFeatureAreas(tile, getSquareSize(), locations);
        }
    }


    private boolean confirmFarmPlacement() {
        String options[] = {_("Place a follower"), _("Cancel") };
        int result = JOptionPane.showOptionDialog(getClient(),
                _("Do you really want to place a follower on farm?"),
                _("Confirm follower placement"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return JOptionPane.YES_OPTION == result;
    }

    private boolean confirmTowerPlacement(Position pos) {
        int result;
        Player activePlayer = getClient().getGame().getActivePlayer();
        if (getClient().getGame().getCapability(TowerCapability.class).getTowerPieces(activePlayer) > 0) {
            String options[] = {
                _("Confirm follower placement"),
                _("Cancel"),
                _("Place a tower piece")
            };
            result = JOptionPane.showOptionDialog(getClient(),
                _("Do you really want to place a follower on the tower?\n(To prevent tower from adding more pieces on the top)"),
                _("Confirm follower placement"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        } else {
            String options[] = {
                _("Confirm follower placement"),
                _("Cancel")
            };
            result = JOptionPane.showOptionDialog(getClient(),
                _("Do you really want to place a follower on the tower?\n(To prevent tower from adding more pieces on the top)"),
                _("Confirm follower placement"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        if (result == JOptionPane.CANCEL_OPTION) {
            //place tower piece instead
            for (PlayerAction action : getClient().getControlPanel().getActionPanel().getActions()) {
                if (action instanceof TowerPieceAction) {
                    ((TowerPieceAction) action).perform(getClient().getServer(), pos);
                    gridPanel.removeLayer(this);
                    return false;
                }
            }
            return false;
        }
        return result == JOptionPane.YES_OPTION;
    }

    private List<Meeple> getDistinctFeatureMeeples(PlayerRestriction allowed, Feature feature) {
        //little optimalization, almost all time size is 1
        if (feature.getMeeples().size() == 1) {
            return feature.getMeeples();
        }
        Set<String> used = new HashSet<>();
        List<Meeple> result = new ArrayList<>();
        for (Meeple m : feature.getMeeples()) {
            if (!allowed.isAllowed(m.getPlayer())) continue;
            String key = m.getPlayer().getIndex() + m.getClass().getSimpleName();
            if (!used.contains(key)) {
                result.add(m);
                used.add(key);
            }
        }
        return result;
    }

    @Override
    protected void performAction(final Position pos, final Location loc) {
        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;
            Feature piece = gridPanel.getTile(pos).getFeature(loc);
            if (piece instanceof Farm) {
                if (Follower.class.isAssignableFrom(ma.getMeepleType()) && getClient().getSettings().isConfirmFarmPlacement()) {
                    if (!confirmFarmPlacement()) return;
                }
            } else if (piece instanceof Tower) {
                if (getClient().getSettings().isConfirmTowerPlacement()) {
                    if (!confirmTowerPlacement(pos)) return;
                }
            }
        }
        if (action instanceof SelectFollowerAction) {
            final SelectFollowerAction selectFollowerAction = (SelectFollowerAction) action;
            List<Meeple> meeples = getDistinctFeatureMeeples(selectFollowerAction.getPlayers(), getGame().getBoard().get(pos).getFeature(loc));
            if (meeples.size() > 1) {
                new AmbiguousUndeployDialog(getClient(), meeples, new AmbiguousUndeployDialogEvent() {
                    @Override
                    public void meepleTypeSelected(Meeple meeple) {
                       selectFollowerAction.perform(getClient().getServer(), pos, loc, meeple.getClass(), meeple.getPlayer());
                    }
                });
                return;
            }
        }
        action.perform(getClient().getServer(), pos, loc);
        return;
    }


}
