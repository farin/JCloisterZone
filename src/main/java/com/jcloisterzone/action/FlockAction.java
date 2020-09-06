package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;

public class FlockAction extends AbstractPlayerAction<Void> {

	private MeeplePointer shepherdPointer;

	public FlockAction(MeeplePointer shepherdPointer) {
		super(null);
		this.shepherdPointer = shepherdPointer;
	}

	public MeeplePointer getShepherdPointer() {
		return shepherdPointer;
	}
}
