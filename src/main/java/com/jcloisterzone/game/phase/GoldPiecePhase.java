package com.jcloisterzone.game.phase;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcloisterzone.action.GoldPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.GoldChangeEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.ui.GameController;

@RequiredCapability(GoldminesCapability.class)
public class GoldPiecePhase extends Phase {

    public GoldPiecePhase(GameController gc) {
        super(gc);
    }

    @Override
    public void enter() {
        if (getTile().hasTrigger(TileTrigger.GOLDMINE)) {
            Position pos = getTile().getPosition();
            int count = gldCap.addGold(pos);
            game.post(new GoldChangeEvent(getActivePlayer(), pos, count-1, count));
        }
        reenter();
    }

    @Override
    public void reenter() {
        if (getTile().hasTrigger(TileTrigger.GOLDMINE)) {
            Position pos = getTile().getPosition();
            GoldPieceAction action = new GoldPieceAction();
            action.addAll(Lists.transform(getBoard().getAdjacentAndDiagonalTiles2(pos), new Function<Tile, Position>() {
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

    @Override
    public void placeGoldPiece(Position pos) {
        //TODO nice to have validate position
        int count = gldCap.addGold(pos);
        game.post(new GoldChangeEvent(getActivePlayer(), pos, count-1, count));
        next();
    }
}
