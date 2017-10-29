package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.game.Rule;

import io.vavr.collection.Map;

public interface RulesMixin {

    Map<Rule, Object> getRules();

    default boolean getBooleanValue(Rule rule) {
        assert rule.getType().equals(Boolean.class);
        return (Boolean) getRules().get(rule).getOrElse(Boolean.FALSE);
    }

}
