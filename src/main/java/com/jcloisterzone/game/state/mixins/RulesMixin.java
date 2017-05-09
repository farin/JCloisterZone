package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.game.CustomRule;

import io.vavr.collection.Map;

public interface RulesMixin {

    Map<CustomRule, Object> getRules();

    default boolean getBooleanValue(CustomRule rule) {
        assert rule.getType().equals(Boolean.class);
        return (Boolean) getRules().get(rule).getOrElse(Boolean.FALSE);
    }

}
