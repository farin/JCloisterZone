package com.jcloisterzone.ai;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;

public class NotSupportedInteraction implements UserInterface {

    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        throw new UnsupportedOperationException();
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

    @Override
    public void showWarning(String title, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void chatMessage(Player player, String message) {
        //empty
    }

}