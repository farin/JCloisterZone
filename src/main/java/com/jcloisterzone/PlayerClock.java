package com.jcloisterzone;

import java.io.Serializable;

public class PlayerClock implements Serializable {

    private static final long serialVersionUID = 1L;

    private long time;
    private boolean running;
    private long runningSince;

    public PlayerClock() {
        this(0);
    }

    /**
     * @param time in ms
     */
    public PlayerClock(long time) {
        this(time, false, 0);
    }

    public PlayerClock(long time, boolean running, long runningSince) {
        this.time = time;
        this.running = running;
        this.runningSince = runningSince;
    }


    /**
     * @return playtime in ms
     */
    public long getTime() {
        if (running) {
            return time + System.currentTimeMillis() - runningSince;
        } else {
            return time;
        }
    }

    public void setRunning(boolean setRunning) {
        // start running
        if (!this.running && setRunning) {
            this.running = true;
            this.runningSince = System.currentTimeMillis();
        }
        // stop running
        if (this.running && !setRunning){
            this.running = false;
            time += System.currentTimeMillis() - runningSince;
        }
    }
}
