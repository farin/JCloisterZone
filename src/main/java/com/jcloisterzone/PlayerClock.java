package com.jcloisterzone;

import java.io.Serializable;

@Immutable
public class PlayerClock implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long time;
    private final boolean running;
    private final long runningSince;

    public PlayerClock(long time) {
        this(time, false, 0);
    }

    public PlayerClock(long time, boolean running) {
        this(time, running, running ? System.currentTimeMillis() : 0);
    }

    public PlayerClock(long time, boolean running, long runningSince) {
        this.time = time;
        this.running = running;
        this.runningSince = runningSince;
    }


    public long getTime() {
        if (running) {
            return time + System.currentTimeMillis() - runningSince;
        } else {
            return time;
        }
    }

    /**
     * Returns time ignoring runningSince (which is reset to now)
     */
    public PlayerClock resetRunning() {
        return new PlayerClock(time, running, System.currentTimeMillis());
    }

    public PlayerClock setTime(long time) {
        return new PlayerClock(time, running,
            running ? System.currentTimeMillis() : 0) ;
    }

    public boolean isRunning() {
        return running;
    }

    public PlayerClock setRunning(boolean running) {
        return new PlayerClock(time, running, running ? System.currentTimeMillis() : 0);
    }
}
