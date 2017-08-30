package com.jcloisterzone.action;

import java.io.Serializable;

import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

public abstract class PlayerAction<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final Set<T> options;

    public PlayerAction(Set<T> options) {
       this.options = options;
    }

    public abstract WsInGameMessage select(T option);

    @Override
    public Iterator<T> iterator() {
        return options.iterator();
    }

    public Set<T> getOptions() {
        return options;
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }
}
