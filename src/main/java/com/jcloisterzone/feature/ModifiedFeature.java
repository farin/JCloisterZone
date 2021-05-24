package com.jcloisterzone.feature;

import com.jcloisterzone.feature.modifier.BooleanOrModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.util.ArrayList;

public interface ModifiedFeature<C extends ModifiedFeature> extends Feature {

    Map<FeatureModifier<?>, Object> getModifiers();
    C setModifiers(Map<FeatureModifier<?>, Object> modifiers);

    default <T> C putModifier(FeatureModifier<T> modifier, T value) {
        return setModifiers(getModifiers().put((FeatureModifier<?>) modifier, (Object) value));
    }

    default boolean hasModifier(BooleanOrModifier modifier) {
        return getModifier(modifier, false);
    }

    default <T> T getModifier(FeatureModifier<T> modifier, T defaultValue) {
        return (T) getModifiers().get((FeatureModifier<?>) modifier).getOrElse(defaultValue);
    }

    default Map<FeatureModifier<?>, Object> mergeModifiers(ModifiedFeature<C> other) {
        return mergeModifiers(other.getModifiers());
    }

    default Map<FeatureModifier<?>, Object> mergeModifiers(Map<FeatureModifier<?>, Object> otherModifiers) {
        var modifiers = getModifiers();
        var missingOtherKeys = otherModifiers.keySet().diff(modifiers.keySet());

        ArrayList<Tuple2<FeatureModifier<?>, Object>> entries = new ArrayList();
        for (Tuple2<FeatureModifier<?>, Object> t: modifiers) {
            var otherValue = otherModifiers.get(t._1).getOrNull();
            if (otherValue == null) {
                entries.add(t);
            } else {
                Object val = ((FeatureModifier<Object>)t._1).mergeValues(t._2, otherValue);
                if (val != null) {
                    entries.add(t.update2(val));
                }
            }
        }

        for (FeatureModifier<?> mod : missingOtherKeys) {
            Object val = otherModifiers.get(mod).get();
            entries.add(new Tuple2<>(mod, val));
        }
        return HashMap.ofEntries(entries);
    }
}
