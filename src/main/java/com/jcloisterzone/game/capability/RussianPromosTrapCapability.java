package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.SoloveiRazboynik;
import com.jcloisterzone.figure.Abbot;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class RussianPromosTrapCapability extends Capability<Void> {

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "razboynik").isEmpty()) {
            SoloveiRazboynik razboynik = new SoloveiRazboynik();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(Location.TOWER, razboynik));
        }
        return tile;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();
        HashSet places = HashSet.empty();
        Player active = state.getActivePlayer();
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple meeple = t._1;
            FeaturePointer fp = t._2;
            if (meeple.getPlayer().equals(active) && state.getFeature(fp) instanceof SoloveiRazboynik) {
                places = places.add(new MeeplePointer(fp, meeple.getId()));
            }
        }

        if (!places.isEmpty()){
            actions = actions.appendAction(new ReturnMeepleAction(places, ReturnMeepleMessage.ReturnMeepleSource.TRAP));
            state = state.setPlayerActions(actions);
        }
        return state;
    }
}
