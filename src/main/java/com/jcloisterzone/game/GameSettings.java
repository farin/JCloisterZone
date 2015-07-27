package com.jcloisterzone.game;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.Expansion;

public class GameSettings {

    private final String gameId;
    private String name;
    private final EnumMap<CustomRule, Object> customRules = new EnumMap<>(CustomRule.class);
    private final Set<Expansion> expansions = EnumSet.noneOf(Expansion.class);
    private final Set<Class<? extends Capability>> capabilityClasses = new HashSet<>();


    public GameSettings(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasExpansion(Expansion expansion) {
        return expansions.contains(expansion);
    }

    public boolean getBooleanValue(CustomRule rule) {
        assert rule.getType().equals(Boolean.class);
        if (!customRules.containsKey(rule)) return false;
        return (Boolean) customRules.get(rule);
    }

    public boolean hasCapability(Class<? extends Capability> c) {
        return capabilityClasses.contains(c);
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public EnumMap<CustomRule, Object> getCustomRules() {
        return customRules;
    }

    public Set<Class<? extends Capability>> getCapabilityClasses() {
        return capabilityClasses;
    }
}
