package com.jcloisterzone.game;

import java.util.EnumSet;
import java.util.Set;

import com.jcloisterzone.Expansion;


public class GameSettings {

    private final Set<CustomRule> customRules = EnumSet.noneOf(CustomRule.class);
    private final Set<Expansion> expansions = EnumSet.noneOf(Expansion.class);
    private final Set<Capability> capabilities = EnumSet.noneOf(Capability.class);

    public boolean hasExpansion(Expansion expansion) {
        return expansions.contains(expansion);
    }

    public boolean hasRule(CustomRule rule) {
        return customRules.contains(rule);
    }

    public boolean hasCapability(Capability c) {
        return capabilities.contains(c);
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public Set<CustomRule> getCustomRules() {
        return customRules;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

}
