package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.game.Game;

/** just prepares any-time actions */
public class AnyTimeActionPhase extends Phase {

    public AnyTimeActionPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        List<PlayerAction> actions = new ArrayList<>();
        //game.prepareAnyTimeActions(actions);

        next();
    }

}
