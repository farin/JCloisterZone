package com.jcloisterzone.game.setup;

import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.Rule;

public class RuleQuery implements SetupQuery {

    private final Rule rule;
    private final Object value;

    public RuleQuery(Rule rule, Object value) {
        this.rule = rule;
        this.value = value;
    }

    @Override
    public Boolean apply(GameSetup gameSetup) {
        Object value = gameSetup.getRules().get(this.rule).getOrNull();
        return this.value.equals(value);
    }
}
