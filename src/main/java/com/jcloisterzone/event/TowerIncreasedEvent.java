package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;

public class TowerIncreasedEvent extends PlayEvent implements Undoable {

    private final int captureRange;

    public TowerIncreasedEvent(Player player, Position position, int captureRange) {
        super(player, position);
        this.captureRange = captureRange;
    }

    public int getCaptureRange() {
        return captureRange;
    }

    @Override
    public void undo(Game game) {
        Tile tile = game.getBoard().get(getPosition());
        assert tile.getTower().getHeight() > 0;
        tile.getTower().setHeight(tile.getTower().getHeight() - 1);

        TowerCapability cap = game.getCapability(TowerCapability.class);
        cap.setTowerPieces(getPlayer(), cap.getTowerPieces(getPlayer()) + 1);
    }
}
