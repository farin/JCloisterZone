package com.jcloisterzone.game;

public class SnapshotCorruptedException extends RuntimeException {

	public SnapshotCorruptedException() {
		super();
	}

	public SnapshotCorruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SnapshotCorruptedException(String message) {
		super(message);
	}

	public SnapshotCorruptedException(Throwable cause) {
		super(cause);
	}

}
