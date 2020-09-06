package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.event.DoubleTurnEvent;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.List;
import io.vavr.collection.Queue;

public interface EventsMixin {

    Queue<PlayEvent> getEvents();
    GameState setEvents(Queue<PlayEvent> events);

    default GameState appendEvent(PlayEvent ev) {
        return setEvents(getEvents().append(ev));
    }

    default List<PlayEvent> getCurrentTurnEvents() {
        List<PlayEvent> res = List.empty();
        for (PlayEvent ev : getEvents().reverseIterator()) {
            res = res.prepend(ev);
            if (ev instanceof PlayerTurnEvent) {
                break;
            }
        }
        return res;
    }

    default List<PlayEvent> getCurrentTurnPartEvents() {
        List<PlayEvent> res = List.empty();
        for (PlayEvent ev : getEvents().reverseIterator()) {
            res = res.prepend(ev);
            if (ev instanceof PlayerTurnEvent || ev instanceof DoubleTurnEvent) {
                break;
            }
        }
        return res;
    }
}
