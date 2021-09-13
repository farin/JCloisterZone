package com.jcloisterzone.feature;

import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;

public interface ModifiedFeature<C extends ModifiedFeature> extends Feature {

    Map<FeatureModifier<?>, Object> getModifiers();
    C setModifiers(Map<FeatureModifier<?>, Object> modifiers);

    default <T> C putModifier(FeatureModifier<T> modifier, T value) {
        return setModifiers(getModifiers().put((FeatureModifier<?>) modifier, (Object) value));
    }

    default boolean hasModifier(GameState state, BooleanModifier modifier) {
        return getModifier(state, modifier, false);
    }

    default <T> T getModifier(GameState state, FeatureModifier<T> modifier, T defaultValue) {
        if (modifier.getEnabledBy() != null && !modifier.getEnabledBy().apply(state)) {
            return defaultValue;
        }
        return (T) getModifiers().get((FeatureModifier<?>) modifier).getOrElse(defaultValue);
    }

    default Set<FeatureModifier<?>> getScriptedModifiers() {
        return getModifiers().keySet().filter(mod -> mod.getScoringScript() != null);
    }

    default Map<FeatureModifier<?>, Object> mergeModifiers(ModifiedFeature<C> other) {
        return mergeModifiers(other.getModifiers());
    }

    default Map<FeatureModifier<?>, Object> mergeModifiers(Map<FeatureModifier<?>, Object> otherModifiers) {
        var modifiers = getModifiers();
        var missingOtherKeys = otherModifiers.keySet().diff(modifiers.keySet());

        ArrayList<Tuple2<FeatureModifier<?>, Object>> entries = new ArrayList();
        for (Tuple2<FeatureModifier<?>, Object> t: modifiers) {
            var modifier = ((FeatureModifier<Object>)t._1);
            var otherValue = otherModifiers.get(t._1).getOrNull();
            if (otherValue == null) {
                if (!modifier.isExclusive(t._2)) {
                    entries.add(t);
                }
            } else {
                Object val = modifier.mergeValues(t._2, otherValue);
                if (val != null) {
                    entries.add(t.update2(val));
                }
            }
        }

        for (FeatureModifier<?> _mod : missingOtherKeys) {
            var mod = (FeatureModifier<Object>) _mod;
            Object val = otherModifiers.get(mod).get();
            if (!mod.isExclusive(val)) {
                entries.add(new Tuple2<>(mod, val));
            }
        }
        return HashMap.ofEntries(entries);
    }

    default void scoreScriptedModifiers(java.util.List<ExprItem> exprItems, java.util.Map<String, Object> members) {
        Set<FeatureModifier<?>> scriptedModifiers = getScriptedModifiers();
        if (!scriptedModifiers.isEmpty()) {
            try (Context context = Context.create("js")) {
                Value bindings = context.getBindings("js");
                members.forEach((key, value) -> {
                    bindings.putMember(key, value);
                });

                getModifiers().forEach((mod, value) -> {
                    bindings.putMember(mod.getName(), value);
                });

                scriptedModifiers.forEach(mod -> {
                    Value res = context.eval("js", "(() => { " + mod.getScoringScript() + "})()");
                    if (res.hasArrayElements()) {
                        long size = res.getArraySize();
                        for (int i = 0; i < size; i++) {
                            Value item = res.getArrayElement(i);
                            String name = item.getMember("name").asString();
                            int points = item.getMember("points").asInt();
                            Integer count = item.hasMember("count") ? item.getMember("count").asInt() : null;
                            exprItems.add(new ExprItem(count, name, points));
                        }
                    }
                });
            }
        }
    }
}
