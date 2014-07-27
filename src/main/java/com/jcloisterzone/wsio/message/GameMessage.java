package com.jcloisterzone.wsio.message;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;

public class GameMessage {
    private String id;
    private String state;
    private Set<CustomRule> customRules;
    private Set<Expansion> expansions;
    private Set<Class<? extends Capability>> capabilityClasses;
    private String snapshot;
    private SlotMessage[] slots;

    public GameMessage(String id, String state, Set<CustomRule> customRules, Set<Expansion> expansions, Set<Class<? extends Capability>> capabilityClasses) {
        this.id = id;
        this.state = state;
        this.customRules = customRules;
        this.expansions = expansions;
        this.capabilityClasses = capabilityClasses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public SlotMessage[] getSlots() {
        return slots;
    }

    public void setSlots(SlotMessage[] slots) {
        this.slots = slots;
    }


}
