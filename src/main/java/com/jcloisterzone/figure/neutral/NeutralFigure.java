package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.game.Game;

public class NeutralFigure<T extends BoardPointer> extends Figure {

    private static final long serialVersionUID = 3458278495952412845L;

    public NeutralFigure(Game game) {
        super(game);
    }

    public void deploy(T at) {
        if (at instanceof Position) {
            Position origin = getPosition();
            setFeaturePointer(((Position) at).asFeaturePointer());
            game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
        } else {
            FeaturePointer origin = getFeaturePointer();
            setFeaturePointer((FeaturePointer) at);
            game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
        }
    }


    public void undeploy() {
        deploy((T) null);
    }

}
