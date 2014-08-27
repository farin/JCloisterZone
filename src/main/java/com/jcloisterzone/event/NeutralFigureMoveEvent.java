package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;

public class NeutralFigureMoveEvent extends MoveEvent<Position> implements Undoable {

    public static final int DRAGON = 1;
    public static final int FAIRY = 2;

    public NeutralFigureMoveEvent(int type, Player player, Position from, Position to) {
        super(type, player, from, to);
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case FAIRY:
            FairyCapability fCap = game.getCapability(FairyCapability.class);
            fCap.setFairyPosition(getFrom());
            break;
        case DRAGON:
            DragonCapability dCap = game.getCapability(DragonCapability.class);
            dCap.setDragonPosition(getFrom());
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }
}
