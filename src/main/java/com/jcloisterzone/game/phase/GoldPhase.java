package com.jcloisterzone.game.phase;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcloisterzone.action.GoldPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.GoldChangeEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.GoldminesCapability;

public class GoldPhase extends Phase {

    private final GoldminesCapability gldCap;

    public GoldPhase(Game game) {
        super(game);
        gldCap = game.getCapability(GoldminesCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(GoldminesCapability.class);
    }

    @Override
    public void enter() {
        if (getTile().hasTrigger(TileTrigger.GOLDMINE)) {
            Position pos = getTile().getPosition();
            int count = gldCap.addGold(pos);
            game.post(new GoldChangeEvent(getActivePlayer(), pos, count));

            GoldPieceAction action = new GoldPieceAction();
            action.addAll(Lists.transform(getBoard().getAdjacentAndDiagonalTiles(pos), new Function<Tile, Position>() {
                @Override
                public Position apply(Tile t) {
                    return t.getPosition();
                }
            }));
            game.post(new SelectActionEvent(getActivePlayer(), action, false));
        } else {
            next();
        }
    }
}
