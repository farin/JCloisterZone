package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Queue;

public interface EventsStateMixin {

    Queue<PlayEvent> getEvents();
    GameState setEvents(Queue<PlayEvent> events);

    default GameState appendEvent(PlayEvent ev) {
        return setEvents(getEvents().append(ev));
    }

    default Queue<PlayEvent> getCurrentTurnEvents() {
        Queue<PlayEvent> res = Queue.empty();
        for (PlayEvent ev : getEvents().reverseIterator()) {
            res.prepend(ev);
            if (ev instanceof PlayerTurnEvent) {
                break;
            }
        }
        return res;
    }
}
