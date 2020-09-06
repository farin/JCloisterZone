package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.Capability;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import java.io.Serializable;
import java.util.function.Function;

@Immutable
public class CapabilitiesState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Class<? extends Capability<?>>, Capability<?>> capabilities;
    private final Map<Class<? extends Capability<?>>, Object> models;


    @SuppressWarnings({ "unchecked"})
    public static CapabilitiesState createInitial(Seq<Capability<?>> capabilities) {
        return new CapabilitiesState(
            capabilities.toMap(cap -> new Tuple2(cap.getClass(), cap)),
            HashMap.empty()
        );
    }

    public CapabilitiesState(
        Map<Class<? extends Capability<?>>, Capability<?>> capabilities,
        Map<Class<? extends Capability<?>>, Object> models
    ) {
        this.capabilities = capabilities;
        this.models = models;
    }

    public CapabilitiesState setCapabilities(Map<Class<? extends Capability<?>>, Capability<?>> capabilities) {
        if (capabilities == this.capabilities) return this;
        return new CapabilitiesState(
            capabilities, models
        );
    }

    public CapabilitiesState setModels(Map<Class<? extends Capability<?>>, Object> models) {
        if (models == this.models) return this;
        return new CapabilitiesState(
            capabilities, models
        );
    }

    public <M> CapabilitiesState updateModel(Class<? extends Capability<M>> cls, Function<M, M> fn) {
        M model = getModel(cls);
        M newModel = fn.apply(model);
        if (model == newModel) return this;
        return setModels(models.put(cls, newModel));
    }

    public <M> CapabilitiesState setModel(Class<? extends Capability<M>> cls, M model) {
        M oldModel = getModel(cls);
        if (oldModel == model) return this;
        return setModels(models.put(cls, model));
    }

    public Map<Class<? extends Capability<?>>, Capability<?>> getCapabilities() {
        return capabilities;
    }

    @SuppressWarnings("unchecked")
    public <C extends Capability<?>> C get(Class<C> cls) {
        return (C) capabilities.get(cls).getOrNull();
    }

    public boolean contains(Class<? extends Capability<?>> cls) {
        return capabilities.containsKey(cls);
    }

    public Map<Class<? extends Capability<?>>, Object> getModels() {
        return models;
    }

    @SuppressWarnings("unchecked")
    public <M> M getModel(Class<? extends Capability<M>> class1) {
        return (M) models.get(class1).getOrNull();
    }

    public Seq<Capability<?>> toSeq() {
        return capabilities.values();
    }

    @Override
    public String toString() {
        return String.join(", ", toSeq().map(cap -> cap.toString()));
    }

}
