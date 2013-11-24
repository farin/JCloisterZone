package com.jcloisterzone.ai.legacyplayer;

import com.jcloisterzone.ai.AiScoreContext;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.game.Game;

public class LegacyAiFarmScoreContext extends FarmScoreContext implements AiScoreContext {

    //private final Map<Feature, AiScoreContext> scoreCache;
    private boolean valid;

    public LegacyAiFarmScoreContext(Game game/*, Map<Feature, AiScoreContext> scoreCache*/) {
        super(game);
        //this.scoreCache = scoreCache;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean visit(Feature feature) {
        //scoreCache.put(feature, this);
        return super.visit(feature);
    }

}
