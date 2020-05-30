package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.NeutralFigureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

import io.vavr.collection.List;
import io.vavr.collection.Set;

@RequiredCapability(CountCapability.class)
public class CocCountPhase extends Phase {

    public CocCountPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();
        Count count = state.getNeutralFigures().getCount();
        Position quarterPos = state.getCapabilityModel(CountCapability.class).getQuarterPosition();
        FeaturePointer countFp = state.getNeutralFigures().getCountDeployment();

        List<Location> quarters = Location.QUARTERS.filter(loc -> loc != countFp.getLocation());
        if (!state.getBooleanValue(Rule.FARMERS)) {
            quarters = quarters.remove(Location.QUARTER_MARKET);
        }
        Set<FeaturePointer> options = quarters.map(loc -> new FeaturePointer(quarterPos, loc)).toSet();
        NeutralFigureAction action = new NeutralFigureAction(count, options);

        state = state.setPlayerActions(new ActionsState(player, action, true));
        return promote(state);
    }

    @PhaseMessageHandler
    public StepResult handleMoveNeutralFigure(GameState state, MoveNeutralFigureMessage msg) {
        NeutralFigure<?> fig = state.getNeutralFigures().getById(msg.getFigureId());
        if (fig instanceof Count) {
            Count count = (Count) fig;
            FeaturePointer fp = (FeaturePointer) msg.getTo();

            state = (new MoveNeutralFigure<FeaturePointer>(count, fp, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            return next(state);
        } else {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }
    }

}
