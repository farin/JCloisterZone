package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class MoveEvent<T> extends PlayEvent {

    final T from, to;

    public MoveEvent(Player player, T from, T to) {
        this(0, player, from, to);
    }

    public MoveEvent(int type, Player player, T from, T to) {
        super(type, player);
        this.from = from;
        this.to = to;
    }

    public T getFrom() {
        return from;
    }

    public T getTo() {
        return to;
    }

    @Override
    public String toString() {
        return super.toString() + " player:" +getPlayer() + " from:" + from + " to:" + to;
    }

}
