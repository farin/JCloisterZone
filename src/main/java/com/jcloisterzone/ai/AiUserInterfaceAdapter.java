package com.jcloisterzone.ai;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;

/**
 * Dummy fallback prevent application freezing for some AiPlayers implementation errors
 */
public class AiUserInterfaceAdapter implements UserInterface {

    private AiPlayer aiPlayer;

    public AiUserInterfaceAdapter(AiPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    public AiPlayer getAiPlayer() {
        return aiPlayer;
    }

    public void setAiPlayer(AiPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        if (aiPlayer.isAiPlayerActive()) {
            try {
                aiPlayer.selectAction(actions, canPass);
            } catch (Exception e) {
                aiPlayer.handleRuntimeError(e);
                aiPlayer.selectDummyAction(actions, canPass);
            }
        }
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

    public void chatMessage(Player player, String message) {
    }

    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        if (aiPlayer.isAiPlayerActive()) {
            try {
                aiPlayer.selectDragonMove(positions, movesLeft);
            } catch (Exception e) {
                aiPlayer.handleRuntimeError(e);
                aiPlayer.selectDummyDragonMove(positions, movesLeft);
            }
        }
    }

    @Override
    public void showWarning(String title, String message) {
        aiPlayer.showWarning(title, message);
    }

}
