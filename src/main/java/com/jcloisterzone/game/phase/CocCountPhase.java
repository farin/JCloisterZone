package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.RmiProxy;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

@RequiredCapability(CountCapability.class)
public class CocCountPhase extends Phase {

    public CocCountPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    public void enter() {
        Player activePlayer = game.getActivePlayer();
        Position pos = countCap.getQuarterPosition();
        Location countLoc = countCap.getCount().getLocation();

        //TODO neutral meeple action. see MagaAndWitch
        SelectFeatureAction action = new SelectFeatureAction("count") {
            @Override
            public void perform(RmiProxy server, FeaturePointer target) {
                server.moveNeutralFigure(target, Count.class);
            }
        };

        for (Location quarter : Location.QUARTERS) {
            if (countLoc != quarter) {
                action.add(new FeaturePointer(pos, quarter));
            }
        }

        List<PlayerAction<?>> actions = Collections.singletonList(action);
        game.post(new SelectActionEvent(activePlayer, actions, true));
    }

    @WsSubscribe
    public void handleMoveNeutralFigure(MoveNeutralFigureMessage msg) {
        GameState state = game.getState();
        NeutralFigure<?> fig = state.getNeutralFigures().getById(msg.getFigureId());
        if (fig instanceof Count) {
            Count count = (Count) fig;
            FeaturePointer fp = (FeaturePointer) msg.getTo();

            state = (new MoveNeutralFigure<FeaturePointer>(count, fp, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            next(state);
        } else {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }
    }

}
