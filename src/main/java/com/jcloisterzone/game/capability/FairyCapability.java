package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Set;

@Immutable
public class FairyCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    public static final int FAIRY_POINTS_BEGINNING_OF_TURN = 1;
    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapNeutralFigures(nf -> nf.setFairy(new Fairy("fairy.1")));
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        boolean fairyOnTile = state.getBooleanValue(Rule.FAIRY_ON_TILE);
        Player activePlayer = state.getPlayerActions().getPlayer();


        LinkedHashMap<Follower, FeaturePointer> followers =
            state.getDeployedMeeples()
                .filter((m, fp) -> (m instanceof Follower) && m.getPlayer().equals(activePlayer))
                .mapKeys(m -> (Follower) m);

        Fairy fairy = state.getNeutralFigures().getFairy();

        if (fairyOnTile) {
            Set<Position> options = followers.values().map(fp -> fp.getPosition()).toSet();
            if (options.isEmpty()) {
                return state;
            }
            return state.appendAction(new FairyOnTileAction(fairy.getId(), options));
        } else {
            Set<MeeplePointer> options = followers.map(t -> new MeeplePointer(t)).toSet();
            if (options.isEmpty()) {
                return state;
            }
            return state.appendAction(new FairyNextToAction(fairy.getId(), options));
        }
    }
}
