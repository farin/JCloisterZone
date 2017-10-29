package com.jcloisterzone.event.setup;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.Rule;

public class RuleChangeEvent extends Event {

    private final Rule rule;
    private final Object value;

    public RuleChangeEvent(Rule rule, Object value) {
        this.rule = rule;
        this.value = value;
    }

    public Rule getRule() {
        return rule;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " " + rule.name() + " " + value;
    }

}
