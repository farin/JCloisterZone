package com.jcloisterzone.ui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.figure.Meeple;


public class InvokeInSwingUiAdapter {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final EventBus uiEventBus;

    public InvokeInSwingUiAdapter(EventBus eventBus) {
        this.uiEventBus = eventBus;
    }

    @Subscribe public void handleAllEvents(Event event) {
        logger.info("event: {}", event);
        final Object freezedEvent = freezeEvent(event);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                uiEventBus.post(freezedEvent);
            }
        });
    }

    private Event freezeEvent(Event ev) {
        if (ev instanceof MeepleEvent) {
            //TODO is it really needed with new meeple events?
            MeepleEvent mev = (MeepleEvent) ev;
            Meeple m = mev.getMeeple();
            return new MeepleEvent((Meeple) m.clone(), mev.getFrom(), mev.getTo());
        }
        return ev;
    }
}
