package com.jcloisterzone.ai.choice;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.game.Game;

public class TilePlacementChoice extends ActionChoice<TilePlacement> {

    public TilePlacementChoice(AiChoice previous, SavePoint savePoint, PlayerAction<TilePlacement> action, TilePlacement value) {
        super(previous, savePoint, action, value);
    }

    @Override
    public void rankPartial(GameRanking gr, Game game) {
        Tile tile = game.getBoard().get(getValue().getPosition());
        this.setRanking(getRanking() + gr.getPartialAfterTilePlacement(game, tile));
    }

}
