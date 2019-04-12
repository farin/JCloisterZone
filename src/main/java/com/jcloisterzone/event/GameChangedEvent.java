package com.jcloisterzone.event;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Queue;
import io.vavr.collection.Stream;

//temporary event for transition to new architecture?
public class GameChangedEvent extends Event {

    private final GameState prev;
    private final GameState curr;

    private transient Queue<PlayEvent> newEvents;
    private transient Queue<PlayEvent> removedEvents;

    public GameChangedEvent(GameState prev, GameState curr) {
        this.prev = prev;
        this.curr = curr;
    }

    public GameState getCurrentState() {
        return curr;
    }

    public GameState getPrevState() {
        return prev;
    }

    public Queue<PlayEvent> getNewPlayEvents() {
        if (newEvents == null) {
            if (prev == null || prev.getEvents() == null) {
                newEvents = curr.getEvents();
            } else {
                newEvents = curr.getEvents().removeAll(prev.getEvents());
            }
        }
        return newEvents;
    }

    public Queue<PlayEvent> getRemovedPlayEvents() {
        if (removedEvents == null) {
            if (prev == null || prev.getEvents() == null) {
                removedEvents = Queue.empty();
            } else {
                removedEvents = prev.getEvents().removeAll(curr.getEvents());
            }
        }
        return removedEvents;
    }

    public Stream<PlayEvent> getPlayEventsSymmetricDifference() {
        return Stream.concat(getNewPlayEvents(), getRemovedPlayEvents());
    }

    public boolean hasPlacedTilesChanged() {
        return prev.getPlacedTiles() != curr.getPlacedTiles();
    }

    public boolean hasTilePackChanged() {
        return prev.getTilePack() != curr.getTilePack();
    }

    public boolean hasPlayerActionsChanged() {
        return prev.getPlayerActions() != curr.getPlayerActions();
    }

    public boolean hasTurnPlayerChanged() {
        return !prev.getTurnPlayer().equals(curr.getTurnPlayer());
    }

    public boolean hasDiscardedTilesChanged() {
        return prev.getDiscardedTiles() != curr.getDiscardedTiles();
    }

    public boolean hasMeeplesChanged() {
        return prev.getDeployedMeeples() != curr.getDeployedMeeples();
    }

    public boolean hasNeutralFiguresChanged() {
        return prev.getNeutralFigures().getDeployedNeutralFigures() !=
            curr.getNeutralFigures().getDeployedNeutralFigures();
    }
}
