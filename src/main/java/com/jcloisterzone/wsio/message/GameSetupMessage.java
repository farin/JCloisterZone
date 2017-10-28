package com.jcloisterzone.wsio.message;

import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesSetAdapter;
import com.jcloisterzone.wsio.message.adapters.ExpansionMapAdapter;
import com.jcloisterzone.wsio.message.adapters.RulesMapAdapter;

@WsMessageCommand("GAME_SETUP")
public class GameSetupMessage implements WsMessage, WsInGameMessage  {
    private String gameId;
    @JsonAdapter(RulesMapAdapter.class)
    private Map<Rule, Object> rules;
    @JsonAdapter(ExpansionMapAdapter.class)
    private Map<Expansion, Integer> expansions;
    @JsonAdapter(CapabilitiesSetAdapter.class)
    private Set<Class<? extends Capability<?>>> capabilities;

    public GameSetupMessage() {
    }

    public GameSetupMessage(Map<Rule, Object> rules, Set<Class<? extends Capability<?>>> capabilities, Map<Expansion, Integer> expansions) {
        this.rules = rules;
        this.expansions = expansions;
        this.capabilities = capabilities;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Map<Rule, Object> getRules() {
        return rules;
    }

    public void setRules(Map<Rule, Object> rules) {
        this.rules = rules;
    }

    public Map<Expansion, Integer> getExpansions() {
        return expansions;
    }

    public void setExpansions(Map<Expansion, Integer> expansions) {
        this.expansions = expansions;
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
        this.capabilities = capabilities;
    }

}
