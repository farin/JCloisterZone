package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;

public class NeutralFigureMoveEvent extends MoveEvent<BoardPointer> implements Undoable {

    private final NeutralFigure figure;

    public NeutralFigureMoveEvent(Player player, NeutralFigure figure, BoardPointer from, BoardPointer to) {
        super(player, from, to);
        this.figure = figure;
    }

    public NeutralFigure getFigure() {
        return figure;
    }

    @Override
    public void undo(Game game) {
        if (figure instanceof Fairy) {
            FairyCapability fCap = game.getCapability(FairyCapability.class);
            if (getFrom() instanceof FeaturePointer) {
                fCap.getFairy().setFeaturePointer((FeaturePointer) getFrom());
            } else if (getFrom() instanceof MeeplePointer) {
                MeeplePointer mp = (MeeplePointer) getFrom();
                fCap.getFairy().setFeaturePointer(mp.asFeaturePointer());
                fCap.getFairy().setNextTo((Follower) game.getMeeple(mp));
            }
        } else if (figure instanceof Dragon) {
            DragonCapability dCap = game.getCapability(DragonCapability.class);
            dCap.getDragon().setFeaturePointer((FeaturePointer) getFrom());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Event getInverseEvent() {
        return new NeutralFigureMoveEvent(getTriggeringPlayer(), figure,  to, from);
    }
}
