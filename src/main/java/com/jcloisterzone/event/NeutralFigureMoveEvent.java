package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;

public class NeutralFigureMoveEvent extends MoveEvent<FeaturePointer> implements Undoable {

    private final NeutralFigure figure;

    public NeutralFigureMoveEvent(Player player, NeutralFigure figure, FeaturePointer from, FeaturePointer to) {
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
            fCap.getFairy().setFeaturePointer(getFrom());
    	} else if (figure instanceof Dragon) {
    		DragonCapability dCap = game.getCapability(DragonCapability.class);
            dCap.getDragon().setFeaturePointer(getFrom());
    	} else {
    		throw new UnsupportedOperationException();
    	}
    }

    @Override
    public Event getInverseEvent() {
        return new NeutralFigureMoveEvent(getTriggeringPlayer(), figure,  to, from);
    }
}
