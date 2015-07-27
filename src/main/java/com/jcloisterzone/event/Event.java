package com.jcloisterzone.event;


/**
 * Ancestor for all events including non-game events like setup and chat.
 */
public abstract class Event {

    private final int type;
    /* flag if event is inverse event triggered by undo */
    private boolean undo;

    public Event() {
        this(0);
    }

    public Event(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
    	if (type != 0) {
    		return getClass().getSimpleName() + "/" + type;
    	}
    	return getClass().getSimpleName();
    }

	public boolean isUndo() {
		return undo;
	}

	public void setUndo(boolean undo) {
		this.undo = undo;
	}
}
