package com.jcloisterzone.ui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.event.Event;


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
        SwingUtilities.invokeLater(() -> {
            uiEventBus.post(event);
        });
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }
}
