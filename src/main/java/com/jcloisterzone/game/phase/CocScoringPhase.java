package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

@RequiredCapability(CountCapability.class)
public class CocScoringPhase extends AbstractCocScoringPhase {

    public CocScoringPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_BLACKSMITH, Location.QUARTER_CASTLE, Location.QUARTER_CATHEDRAL);
    }

}
