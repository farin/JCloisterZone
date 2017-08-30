package com.jcloisterzone.ui.grid.layer;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
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


    @Override
    protected void performAction(BoardPointer ptr) {
        SelectFeatureAction action = getAction();
        FeaturePointer fp = (FeaturePointer) ptr;
        if (action instanceof MeepleAction) {
            MeepleAction ma = (MeepleAction) action;

            if (fp.getLocation() == Location.FLIER) {
                gc.getConnection().send(new DeployFlierMessage(ma.getMeepleType()));
                return;
            }
            //TODO id CLOISTER or ABBOT, check if action contians both for give pos and display dialog if needed

//            if (fp.getLocation() == Location.CLOISTER && abbotOption.contains(fp.getPosition())) {
//                String[] options;
//                boolean abbotOnlyOptionValue = abbotOption.contains(fp.getPosition());
//                if (abbotOnlyOptionValue) {
//                    options = new String[] {_("Place as abbot")};
//                } else {
//                    options = new String[] {_("Place as monk"), _("Place as abbot") };
//                }
//                int result = JOptionPane.showOptionDialog(getClient(),
//                    _("How do you want to place follower on monastery?"),
//                    _("Monastery"),
//                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//                if (result == -1) { //closed dialog
//                    return;
//                }
//                if (abbotOnlyOptionValue || result == JOptionPane.NO_OPTION) {
//                    fp = new FeaturePointer(fp.getPosition(), Location.ABBOT);
//                }
//            }
        }
        gc.getConnection().send(action.select(fp));
        return;
    }


}
