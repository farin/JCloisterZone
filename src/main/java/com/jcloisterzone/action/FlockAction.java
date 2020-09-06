package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.io.message.FlockMessage;
import com.jcloisterzone.io.message.FlockMessage.FlockOption;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.HashSet;

import java.util.Arrays;

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
