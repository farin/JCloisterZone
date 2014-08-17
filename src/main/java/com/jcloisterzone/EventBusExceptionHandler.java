package com.jcloisterzone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

public class EventBusExceptionHandler implements SubscriberExceptionHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final String name;

    public EventBusExceptionHandler(String name) {
        this.name = name;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        logger.error(name + " > Could not dispatch event: " + context.getSubscriber() + " to " + context.getSubscriberMethod(), exception);
    }
}
