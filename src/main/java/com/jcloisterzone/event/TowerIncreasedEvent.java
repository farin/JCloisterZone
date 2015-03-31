package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;

public class TowerIncreasedEvent extends PlayEvent implements Undoable {

    private final int captureRange;
    private final Position position;

    public TowerIncreasedEvent(Player triggeringplayer, Position position, int captureRange) {
        super(triggeringplayer, null);
        this.captureRange = captureRange;
        this.position = position;
    }

    public int getCaptureRange() {
        return captureRange;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void undo(Game game) {
        Tile tile = game.getBoard().get(position);
        assert tile.getTower().getHeight() > 0;
        tile.getTower().setHeight(tile.getTower().getHeight() - 1);

        TowerCapability cap = game.getCapability(TowerCapability.class);
        cap.setLastIncreasedTower(null);
        cap.setTowerPieces(getTriggeringPlayer(), cap.getTowerPieces(getTriggeringPlayer()) + 1);
    }

    @Override
    public Event getInverseEvent() {
        throw new UnsupportedOperationException();
    }
}
