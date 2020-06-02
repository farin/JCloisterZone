package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import javax.swing.JOptionPane;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.FerriesAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.ResourceManager;

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


    private static Area NS_FERRY;
    private static Area NW_FERRY;

    //TODO TEMPORARY
    static {
        NS_FERRY = new Area(new Rectangle(400, 50, 160, 900));
        NW_FERRY = new Area(new Rectangle(0, 0, 160, 707));
        NW_FERRY.transform(AffineTransform.getRotateInstance(Math.PI * 0.25));
        NW_FERRY.transform(AffineTransform.getTranslateInstance(510, 10));
    }

    public Area getFerryArea(Location loc) {
        Rotation rot = loc.getRotationOf(Location.NS);
        if (rot != null) {
            Area a = new Area(NS_FERRY);
            a.transform(rot.getAffineTransform(ResourceManager.NORMALIZED_SIZE));
            return a;
        }
        rot = loc.getRotationOf(Location.NW);
        if (rot != null) {
            Area a = new Area(NW_FERRY);
            a.transform(rot.getAffineTransform(ResourceManager.NORMALIZED_SIZE));
            return a;
        }
        //TODO use shapes.xml to define areas ? (but it is too complicated shape)
        throw new IllegalArgumentException("Incorrect location");
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        SelectFeatureAction action = getAction();

        return action.getOptions().toMap(fp -> {
            FeatureArea fa;
            Location loc = fp.getLocation();

            if (action instanceof BridgeAction) {
                fa = rm.getBridgeArea(loc);
            } else if (action instanceof FerriesAction) {
                fa = new FeatureArea(getFerryArea(loc), FeatureArea.DEFAULT_ROAD_ZINDEX);
                fa = fa.setForceAreaColor(new Color(253, 241, 215));
                //fa = fa.setForceAreaColor(Color.WHITE);
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

        if (gc.getActionLock().get()) {
            return;
        }

        if (action instanceof MeepleAction) {
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
    }
}
