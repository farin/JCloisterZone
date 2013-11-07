package com.jcloisterzone.ai;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;

public class DummyAiPlayer extends AiPlayer {

    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        selectDummyAction(actions, canPass);
    }

    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        selectDummyDragonMove(positions, movesLeft);
    }

    @Override
    public void selectBazaarTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeBazaarBid(int supplyIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectBuyOrSellBazaarOffer(int supplyIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectCornCircleOption() {
        throw new UnsupportedOperationException();
    }

}
