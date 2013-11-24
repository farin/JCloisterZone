package com.jcloisterzone.game;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.Expansion;

public class GameSettings {

    private final Set<CustomRule> customRules = EnumSet.noneOf(CustomRule.class);
    private final Set<Expansion> expansions = EnumSet.noneOf(Expansion.class);
    private final Set<Class<? extends Capability>> capabilityClasses = new HashSet<>();

    public boolean hasExpansion(Expansion expansion) {
        return expansions.contains(expansion);
    }

    public boolean hasRule(CustomRule rule) {
        return customRules.contains(rule);
    }

    public boolean hasCapability(Class<? extends Capability> c) {
        return capabilityClasses.contains(c);
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public Set<CustomRule> getCustomRules() {
        return customRules;
    }

    public Set<Class<? extends Capability>> getCapabilityClasses() {
        return capabilityClasses;
    }
}
