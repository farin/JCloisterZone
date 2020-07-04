package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.PigHerdCapability;
import com.jcloisterzone.game.state.mixins.RulesMixin;

import com.jcloisterzone.wsio.message.GameSetupMessage.PlacedTileItem;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

@Immutable
public class GameSetup implements Serializable, RulesMixin {

    private static final long serialVersionUID = 1L;

    private final Map<String, Integer> tileSets;
    private final Map<Class<? extends Meeple>, Integer> meeples;

    private final Map<Rule, Object> rules;
    private final Set<Class<? extends Capability<?>>> capabilities;
    private final List<PlacedTileItem> start;

    public GameSetup(Map<String, Integer> tileSets, Map<Class<? extends Meeple>, Integer> meeples, Set<Class<? extends Capability<?>>> capabilities, Map<Rule, Object> rules, List<PlacedTileItem> start) {
        this.tileSets = tileSets;
        this.meeples = meeples;
        this.capabilities = capabilities;
        this.rules = rules;
        this.start = start;
    }

//    public boolean hasExpansion(Expansion expansion) {
//        return expansions.containsKey(expansion);
//    }

    public Map<String, Integer> getTileSets() {
        return tileSets;
    }

    public GameSetup setTileSets(Map<String, Integer> tileSets) {
        if (this.tileSets == tileSets) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start);
    }

//    public GameSetup mapExpansions(Function<Map<Expansion, Integer>, Map<Expansion, Integer>> mapper) {
//        return setExpansions(mapper.apply(expansions));
//    }

    public Map<Class<? extends Meeple>, Integer> getMeeples() {
        return meeples;
    }

    public GameSetup setMeeples(Map<Class<? extends Meeple>, Integer> meeples) {
        if (this.meeples == meeples) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start);
    }

    @Override
    public Map<Rule, Object> getRules() {
        return rules;
    }

    public GameSetup setRules(Map<Rule, Object> rules) {
        if (this.rules == rules) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start);
    }

    public GameSetup mapRules(Function<Map<Rule, Object>, Map<Rule, Object>> mapper) {
        return setRules(mapper.apply(rules));
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        return capabilities;
    }

    public GameSetup setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
        if (this.capabilities == capabilities) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start);
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

    public List<PlacedTileItem> getStart() {
        return start;
    }
}
