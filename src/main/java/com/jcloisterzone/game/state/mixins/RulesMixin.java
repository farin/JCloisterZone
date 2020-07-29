package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.game.Rule;
import io.vavr.collection.Map;

public interface RulesMixin {

    Map<Rule, Object> getRules();

    default String getStringRule(Rule rule) {
        assert rule.getType().equals(String.class);
        return (String) getRules().get(rule).getOrNull();
    }

    default boolean getBooleanRule(Rule rule) {
        assert rule.getType().equals(Boolean.class);
        return (Boolean) getRules().get(rule).getOrElse(Boolean.FALSE);
    }

}
