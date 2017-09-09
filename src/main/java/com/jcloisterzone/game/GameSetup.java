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

    private final Set<Expansion> expansions;
    private final Map<Rule, Object> rules;

    // for now just cached value derived from expansions
    private Set<Class<? extends Capability<?>>> capabilities;

    public GameSetup(Set<Expansion> expansions, /*Set<Class<? extends Capability<?>>> capabilities,*/ Map<Rule, Object> rules) {
        this.expansions = expansions;
        //this.capabilities = capabilities;
        this.rules = rules;
    }

    public boolean hasExpansion(Expansion expansion) {
        return expansions.contains(expansion);
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public GameSetup setExpansions(Set<Expansion> expansions) {
        if (this.expansions == expansions) return this;
        return new GameSetup(expansions, rules);
    }

    public GameSetup mapExpansions(Function<Set<Expansion>, Set<Expansion>> mapper) {
        return setExpansions(mapper.apply(expansions));
    }

    @Override
    public Map<Rule, Object> getRules() {
        return rules;
    }

    public GameSetup setRules(Map<Rule, Object> rules) {
        if (this.rules == rules) return this;
        return new GameSetup(expansions, rules);
    }

    public GameSetup mapRules(Function<Map<Rule, Object>, Map<Rule, Object>> mapper) {
        return setRules(mapper.apply(rules));
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        if (capabilities != null) {
             return capabilities;
        }

        Set<Class<? extends Capability<?>>> capabilities = Stream.ofAll(expansions)
            .flatMap(exp -> Arrays.asList(exp.getCapabilities()))
            .toSet();

        if (getBooleanValue(Rule.USE_PIG_HERDS_INDEPENDENTLY)) {
            capabilities.add(PigHerdCapability.class);
        }

//        DebugConfig debugConfig = getDebugConfig();
//        if (debugConfig != null && debugConfig.getOff_capabilities() != null) {
//            List<String> offNames =  debugConfig.getOff_capabilities();
//            for (String tok : offNames) {
//                tok = tok.trim();
//                try {
//                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
//                    @SuppressWarnings("unchecked")
//                    Class<? extends Capability<?>> clazz = (Class<? extends Capability<?>>) Class.forName(className);
//                    classes = classes.remove(clazz);
//                } catch (Exception e) {
//                    logger.warn("Invalid capability name: " + tok, e);
//                }
//            }
//        }
        return capabilities;
    }
}
