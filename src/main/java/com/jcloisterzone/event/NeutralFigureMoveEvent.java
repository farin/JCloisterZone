package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;

public class NeutralFigureMoveEvent extends PlayEvent implements Undoable {

    public static final int DRAGON = 1;
    public static final int FAIRY = 2;

    private final Position fromPosition;

    public NeutralFigureMoveEvent(int type, Player player, Position fromPosition, Position position) {
        super(type, player, position);
        this.fromPosition = fromPosition;
    }

    public Position getFromPosition() {
        return fromPosition;
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case FAIRY:
            FairyCapability fCap = game.getCapability(FairyCapability.class);
            fCap.setFairyPosition(fromPosition);
            break;
        case DRAGON:
            DragonCapability dCap = game.getCapability(DragonCapability.class);
            dCap.setDragonPosition(fromPosition);
            break;
        default:
            throw new UnsupportedOperationException();
        }

    }

}
