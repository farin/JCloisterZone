package com.jcloisterzone;


public class PlayerClock {

    private long time;
    private boolean running;

    private long runningSince;


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
    public long resetRunning() {
        runningSince = System.currentTimeMillis();
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        if (running) runningSince = System.currentTimeMillis();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        runningSince = System.currentTimeMillis();
    }
}
