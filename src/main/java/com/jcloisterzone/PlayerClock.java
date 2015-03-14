package com.jcloisterzone;


public class PlayerClock {

    private int time;
    private boolean running;

    private long runningSince;


    public int getTime() {
        if (running) {
            return time + (int)(System.currentTimeMillis() - runningSince);
        } else {
            return time;
        }
    }

    public void setTime(int time) {
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
