package com.jcloisterzone.ui.animation;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public abstract class AbstractAnimation implements Animation {

	protected long nextFrame;

	public long getDelay(TimeUnit unit) {
		assert unit == TimeUnit.NANOSECONDS;
		return (nextFrame - System.currentTimeMillis()) * 1000000 ;
	}

	public long getNextFrameTs() {
		return nextFrame;
	}

	public int compareTo(Delayed o) {
		long aTs = ((Animation) o).getNextFrameTs();
		if (nextFrame < aTs) return -1;
		if (nextFrame > aTs) return 1;
		return 0;
	}

	@Override
	public boolean switchFrame() {
		return false;
	}



}
