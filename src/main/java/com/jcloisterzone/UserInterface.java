package com.jcloisterzone;

import java.util.EventListener;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;


public interface UserInterface extends EventListener {

    void selectAction(List<PlayerAction> actions, boolean canPass);
    void selectBazaarTile();
    void makeBazaarBid(int supplyIndex);
    void selectBuyOrSellBazaarOffer(int supplyIndex);
    void selectCornCircleOption();

    void showWarning(String title, String message);

    //TODO deprecated - use unified interface
    void selectDragonMove(Set<Position> positions, int movesLeft);

    void chatMessage(Player player, String message);



}
