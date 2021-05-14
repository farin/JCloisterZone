package com.jcloisterzone.feature;

import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import io.vavr.Tuple;
import io.vavr.collection.Map;

public interface ModifiedFeature<C extends ModifiedFeature> extends Feature {

    Map<FeatureModifier<Object>, Object> getModifiers();
    C setModifiers(Map<FeatureModifier<Object>, Object> modifiers);

    default <T> C putModifier(FeatureModifier<T> modifier, T value) {
        return setModifiers(getModifiers().put((FeatureModifier<Object>) modifier, (Object) value));
    }

    default boolean hasModifier(BooleanModifier modifier) {
        return getModifier(modifier, false);
    }

    default <T> T getModifier(FeatureModifier<T> modifier, T defaultValue) {
        return (T) getModifiers().get((FeatureModifier<Object>) modifier).getOrElse(defaultValue);
    }

    default Map<FeatureModifier<Object>, Object> mergeModifiers(ModifiedFeature<C> other) {
        return mergeModifiers(other.getModifiers());
    }

    default Map<FeatureModifier<Object>, Object> mergeModifiers(Map<FeatureModifier<Object>, Object> otherModifiers) {
        var modifiers = getModifiers();
        var merged = modifiers.map((mod, value) -> {
            var otherValue = otherModifiers.get(mod).getOrNull();
            if (otherValue != null) {
                return Tuple.of(mod, mod.mergeValues(value, otherValue));
            } else {
                return Tuple.of(mod, value);
            }
        }).filter((mod, value) -> value != null);
        return merged.merge(otherModifiers.filter((mod, value) -> !modifiers.containsKey((FeatureModifier<Object>) mod)));
    }
}
