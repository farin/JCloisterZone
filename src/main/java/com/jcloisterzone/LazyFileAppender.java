package com.jcloisterzone;

import org.apache.log4j.FileAppender;
import org.apache.log4j.spi.LoggingEvent;

public class LazyFileAppender extends FileAppender {

	private boolean activated;

	@Override
	public void activateOptions() {
		//do it lazy
	}

	@Override
	public void append(LoggingEvent event) {
		if (! activated) {
			activated = true;
			super.activateOptions();
		}
		super.append(event);
	}

}
