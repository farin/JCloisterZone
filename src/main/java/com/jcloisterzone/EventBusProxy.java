package com.jcloisterzone;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.event.Event;

public interface EventBusProxy {

	EventBus getEventBus();
	void post(Event event);
}
