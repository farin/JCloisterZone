package com.jcloisterzone.wsio.message;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;

public class GameSetupMessage {
    private String gameId;
    private Set<CustomRule> customRules;
    private Set<Expansion> expansions;
    private Set<Class<? extends Capability>> capabilityClasses;

    public GameSetupMessage(String gameId, Set<CustomRule> customRules, Set<Expansion> expansions, Set<Class<? extends Capability>> capabilityClasses) {
        this.gameId = gameId;
        this.customRules = customRules;
        this.expansions = expansions;
        this.capabilityClasses = capabilityClasses;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Set<CustomRule> getCustomRules() {
        return customRules;
    }

    public void setCustomRules(Set<CustomRule> customRules) {
        this.customRules = customRules;
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public void setExpansions(Set<Expansion> expansions) {
        this.expansions = expansions;
    }

    public Set<Class<? extends Capability>> getCapabilityClasses() {
        return capabilityClasses;
    }

    public void setCapabilityClasses(
            Set<Class<? extends Capability>> capabilityClasses) {
        this.capabilityClasses = capabilityClasses;
    }



}
