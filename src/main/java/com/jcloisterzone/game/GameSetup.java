package com.jcloisterzone.game;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.mixins.RulesMixin;
import com.jcloisterzone.io.message.GameSetupMessage.PlacedTileItem;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.io.Serializable;
import java.util.function.Function;

@Immutable
public class GameSetup implements Serializable, RulesMixin {

    private static final long serialVersionUID = 1L;

    private final Map<String, Integer> tileSets;
    private final Map<Class<? extends Meeple>, Integer> meeples;

    private final Map<Rule, Object> rules;
    private final Set<Class<? extends Capability<?>>> capabilities;
    private final List<PlacedTileItem> start;
    private final List<String> xmls;
    private final List<String> disabledTiles;

    public GameSetup(Map<String, Integer> tileSets, Map<Class<? extends Meeple>, Integer> meeples, Set<Class<? extends Capability<?>>> capabilities, Map<Rule, Object> rules, List<PlacedTileItem> start, List<String> xmls, List<String> disabledTiles) {
        this.tileSets = tileSets;
        this.meeples = meeples;
        this.capabilities = capabilities;
        this.rules = rules;
        this.start = start;
        this.xmls = xmls;
        this.disabledTiles = disabledTiles;
    }


    public Map<String, Integer> getTileSets() {
        return tileSets;
    }

    public GameSetup setTileSets(Map<String, Integer> tileSets) {
        if (this.tileSets == tileSets) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start, xmls, disabledTiles);
    }

    public Map<Class<? extends Meeple>, Integer> getMeeples() {
        return meeples;
    }

    public GameSetup setMeeples(Map<Class<? extends Meeple>, Integer> meeples) {
        if (this.meeples == meeples) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start, xmls, disabledTiles);
    }

    @Override
    public Map<Rule, Object> getRules() {
        return rules;
    }

    public GameSetup setRules(Map<Rule, Object> rules) {
        if (this.rules == rules) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start, xmls, disabledTiles);
    }

    public GameSetup mapRules(Function<Map<Rule, Object>, Map<Rule, Object>> mapper) {
        return setRules(mapper.apply(rules));
    }

    public Set<Class<? extends Capability<?>>> getCapabilities() {
        return capabilities;
    }

    public GameSetup setCapabilities(Set<Class<? extends Capability<?>>> capabilities) {
        if (this.capabilities == capabilities) return this;
        return new GameSetup(tileSets, meeples, capabilities, rules, start, xmls, disabledTiles);
    }

    public GameSetup mapCapabilities(Function<Set<Class<? extends Capability<?>>>, Set<Class<? extends Capability<?>>>> mapper) {
        return setCapabilities(mapper.apply(capabilities));
    }

    public List<PlacedTileItem> getStart() {
        return start;
    }

    public List<String> getXmls() {
        return xmls;
    }

    public List<String> getDisabledTiles() {
        return disabledTiles;
    }

    public boolean contains(Class<? extends Capability<?>> cap) {
        return capabilities.contains(cap);
    }
}
