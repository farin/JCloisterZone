package com.jcloisterzone.ui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.figure.Meeple;


public class InvokeInSwingUiAdapter {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private final EventBus uiEventBus;

    public InvokeInSwingUiAdapter(EventBus eventBus) {
        this.uiEventBus = eventBus;
    }

    @Subscribe
    public void handleAllEvents(Event event) {
        logger.info("event: {}", event);
        if (reportingTool != null) {
            reportingTool.report("event: " + event);
        }
        final Object frozenEvent = freezeEvent(event);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                uiEventBus.post(frozenEvent);
            }
        });
    }

    private Event freezeEvent(Event ev) {
        if (ev instanceof MeepleEvent) {
            //TODO is it really needed with new meeple events?
            MeepleEvent mev = (MeepleEvent) ev;
            Meeple m = mev.getMeeple();
            return new MeepleEvent(((MeepleEvent) ev).getTriggeringPlayer(), (Meeple) m.clone(), mev.getFrom(), mev.getTo());
        }
        return ev;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }
}
