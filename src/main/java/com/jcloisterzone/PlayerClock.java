package com.jcloisterzone;

import java.io.Serializable;

@Immutable
public class PlayerClock implements Serializable {

    private static final long serialVersionUID = 1L;

    private long time;
    private Long runningSince;

    /**
     * @param time in ms
     */
    public PlayerClock(long time) {
        this(time, null);
    }

    public PlayerClock(long time, Long runningSince) {
        this.time = time;
        this.runningSince = runningSince;
    }


    /**
     * @return playtime in ms
     */
    public long getTime(long clock) {
        if (isRunning()) {
            return time + clock - runningSince;
        } else {
            return time;
        }
    }

    public PlayerClock start(long clock) {
        return new PlayerClock(time, clock);
    }

    public PlayerClock stop(long clock) {
        if (!isRunning()) {
            throw new IllegalStateException("Clock is not running");
        }
        return new PlayerClock(time + clock - runningSince, null);
    }


    public boolean isRunning() {
        return runningSince != null;
    }

}
