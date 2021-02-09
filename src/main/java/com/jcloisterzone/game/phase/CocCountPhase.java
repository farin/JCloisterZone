package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.NeutralFigureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleDeployed;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class CocCountPhase extends Phase {

    public CocCountPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();
        Count count = state.getNeutralFigures().getCount();
        Position quarterPos = state.getCapabilityModel(CountCapability.class).getQuarterPosition();
        FeaturePointer countFp = state.getNeutralFigures().getCountDeployment();
        String rule = state.getStringRule(Rule.COUNT_MOVE);

        if ("clockwise".equals(rule)) {
            Location quarter = countFp.getLocation();
            Location nextQuarter = null;
            if (quarter.equals(Location.QUARTER_CASTLE)) nextQuarter = Location.QUARTER_MARKET;
            else if (quarter.equals(Location.QUARTER_MARKET)) nextQuarter = Location.QUARTER_BLACKSMITH;
            else if (quarter.equals(Location.QUARTER_BLACKSMITH)) nextQuarter = Location.QUARTER_CATHEDRAL;
            else nextQuarter = Location.QUARTER_CASTLE;

            state = (new MoveNeutralFigure<FeaturePointer>(count, new FeaturePointer(quarterPos, nextQuarter), player)).apply(state);
            return next(state);
        }

        if ("follow-meeple".equals(rule)) {
            MeepleDeployed cocDeployment = (MeepleDeployed) state.getEvents().findLast(ev -> ev instanceof MeepleDeployed && ((MeepleDeployed)ev).getLocation().isCityOfCarcassonneQuarter()).get();
            state = (new MoveNeutralFigure<FeaturePointer>(count, new FeaturePointer(quarterPos, cocDeployment.getLocation()), player)).apply(state);
            return next(state);
        }

        List<Location> quarters = Location.QUARTERS.filter(loc -> loc != countFp.getLocation());
        if (!state.getBooleanRule(Rule.FARMERS)) {
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
