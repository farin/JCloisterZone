package com.jcloisterzone.event.setup;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.CustomRule;

public class RuleChangeEvent extends Event {

    private final CustomRule rule;
    private final Object value;

    public RuleChangeEvent(CustomRule rule, Object value) {
        assert value != null;
        this.rule = rule;
        this.value = value;
    }

    public CustomRule getRule() {
        return rule;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " " + rule.name() + " " + value.toString();
    }

}
