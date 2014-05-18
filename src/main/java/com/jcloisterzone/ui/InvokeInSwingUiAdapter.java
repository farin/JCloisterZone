package com.jcloisterzone.ui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.figure.Meeple;


public class InvokeInSwingUiAdapter {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final EventBus uiEventBus;

    public InvokeInSwingUiAdapter(EventBus eventBus) {
        this.uiEventBus = eventBus;
    }

    @Subscribe public void handleAllEvents(Event event) {
        logger.debug("Event received {}", event);
        final Object freezedEvent = freezeEvent(event);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                uiEventBus.post(freezedEvent);
            }
        });
    }

    private Event freezeEvent(Event ev) {
        if (ev instanceof MeepleEvent) {
            Meeple m = ((MeepleEvent) ev).getMeeple();
            return new MeepleEvent(ev.getType(), (Meeple) m.clone());

        }
        if (ev instanceof ScoreEvent) {
            ScoreEvent sev = (ScoreEvent) ev;
            if (sev.getMeeple() != null) {
                Meeple m = sev.getMeeple();
                ScoreEvent copy = new ScoreEvent(sev.getFeature(), sev.getPoints(), sev.getCategory(), (Meeple) m.clone());
                copy.setFinal(sev.isFinal());
                return copy;
            }
        }
        return ev;
    }
}
