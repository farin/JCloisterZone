package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.I18nUtils._tr;

import javax.swing.JOptionPane;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.wsio.message.DeployFlierMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


public class FeatureAreaLayer extends AbstractAreaLayer {

    public FeatureAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public SelectFeatureAction getAction() {
        return (SelectFeatureAction) super.getAction();
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        SelectFeatureAction action = getAction();

        return action.getOptions().toMap(fp -> {
            FeatureArea fa;
            Location loc = fp.getLocation();

            if (action instanceof BridgeAction) {
                fa = rm.getBridgeArea(loc);
            } else if ((action instanceof MeepleAction) && ((MeepleAction)action).getMeepleType().equals(Barn.class)) {
                fa = rm.getBarnArea();
            } else {
                //Decouple from gc.getGame(), use state directly
                PlacedTile pt = gc.getGame().getState().getPlacedTile(fp.getPosition());
                fa = rm.getFeatureArea(pt.getTile(), pt.getRotation(), loc);
            }

            return new Tuple2<>(fp, fa.translateTo(fp.getPosition()));
        });
    }

    private Set<Location> getMonasteryOptions(Position pos) {
        return getAction().getOptions()
            .filter(fp -> fp.getPosition() == pos)
            .map(FeaturePointer::getLocation)
            .filter(loc -> loc ==  Location.CLOISTER || loc == Location.MONASTERY);
    }

    @Override
    protected void performAction(BoardPointer ptr) {
        SelectFeatureAction action = getAction();
        FeaturePointer fp = (FeaturePointer) ptr;
        Position pos = fp.getPosition();

        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;

            if (fp.getLocation() == Location.FLYING_MACHINE) {
                gc.getConnection().send(new DeployFlierMessage(fp, ma.getMeepleId()));
                return;
            }

            boolean isMonasteryLocataion = fp.getLocation() == Location.CLOISTER || fp.getLocation() == Location.MONASTERY;

            if (isMonasteryLocataion) {
                Set<Location> monasteryOptions = getMonasteryOptions(pos);
                if (monasteryOptions.contains(Location.MONASTERY)) {
                    String[] dialogOptions;
                    boolean abbotOnly = !monasteryOptions.contains(Location.CLOISTER);
                    if (abbotOnly) {
                        dialogOptions = new String[] {_tr("Place as abbot")};
                    } else {
                        dialogOptions = new String[] {_tr("Place as monk"), _tr("Place as abbot") };
                    }
                    int result = JOptionPane.showOptionDialog(getClient(),
                        _tr("How do you want to place follower on monastery?"),
                        _tr("Monastery"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, dialogOptions, dialogOptions[0]);
                    if (result == -1) { //closed dialog
                        return;
                    }
                    if (abbotOnly || result == JOptionPane.NO_OPTION) {
                        fp = new FeaturePointer(pos, Location.MONASTERY);
                    }
                }
            }
        }
        gc.getConnection().send(action.select(fp));
        return;
    }


}
