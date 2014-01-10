package com.jcloisterzone.event;

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



}
