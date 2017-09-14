package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.capability.PigHerdCapability;
import com.jcloisterzone.game.state.mixins.RulesMixin;

import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

@Immutable
public class GameSetup implements Serializable, RulesMixin {

    private static final long serialVersionUID = 1L;

    private final Map<Expansion, Integer> expansions;
    private final Map<Rule, Object> rules;
    private final Set<Class<? extends Capability<?>>> capabilities;

    public GameSetup(Map<Expansion, Integer> expansions, Set<Class<? extends Capability<?>>> capabilities, Map<Rule, Object> rules) {
        this.expansions = expansions;
        this.capabilities = capabilities;
        this.rules = rules;
    }

    public boolean hasExpansion(Expansion expansion) {
        return expansions.containsKey(expansion);
    }

    public Map<Expansion, Integer> getExpansions() {
        return expansions;
    }

    public GameSetup setExpansions(Map<Expansion, Integer> expansions) {
        if (this.expansions == expansions) return this;
        return new GameSetup(expansions, capabilities, rules);
    }

    public GameSetup mapExpansions(Function<Map<Expansion, Integer>, Map<Expansion, Integer>> mapper) {
        return setExpansions(mapper.apply(expansions));
    }

    @Override
    public Map<Rule, Object> getRules() {
        return rules;
    }

    public GameSetup setRules(Map<Rule, Object> rules) {
        if (this.rules == rules) return this;
        return new GameSetup(expansions, capabilities, rules);
    }

    public GameSetup mapRules(Function<Map<Rule, Object>, Map<Rule, Object>> mapper) {
        return setRules(mapper.apply(rules));
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        return capabilities;
    }

    public GameSetup setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
        if (this.capabilities == capabilities) return this;
        return new GameSetup(expansions, capabilities, rules);
    }

    public GameSetup mapCapabilities(Function<Set<Class<? extends Capability<?>>>, Set<Class<? extends Capability<?>>>> mapper) {
        return setCapabilities(mapper.apply(capabilities));
    }

    public static Set<Class<? extends Capability<?>>> getCapabilitiesForExpansionsAndRules(Map<Expansion, Integer> expansions, Map<Rule, Object> rules) {
        Set<Class<? extends Capability<?>>> capabilities = Stream.ofAll(expansions.keySet())
            .flatMap(exp -> Arrays.asList(exp.getCapabilities()))
            .toSet();

        if ((Boolean) rules.get(Rule.USE_PIG_HERDS_INDEPENDENTLY).getOrElse(Boolean.FALSE)) {
            capabilities = capabilities.add(PigHerdCapability.class);
        }

        return capabilities;
    }
}
