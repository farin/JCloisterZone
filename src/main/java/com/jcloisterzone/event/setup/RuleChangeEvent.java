package com.jcloisterzone.event.setup;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.CustomRule;

public class RuleChangeEvent extends Event {

    private final CustomRule rule;
    private final boolean enabled;

    public RuleChangeEvent(CustomRule rule, boolean enabled) {
        this.rule = rule;
        this.enabled = enabled;
    }

    public CustomRule getRule() {
        return rule;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return super.toString() + " " + rule.name() + (enabled ? " on" : " off");
    }

}
