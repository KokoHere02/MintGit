package com.mintgit.exception;

import com.mintgit.core.ObjectId;

public class CorruptObjectException extends MintGitException {


	public CorruptObjectException(ObjectId id, String message) {
		super(id != null
			? "corrupt object " + id + ": " + message
			: "corrupt commit object: " + message);
		this.id = id;
	}

	public CorruptObjectException(ObjectId id) {
		super("corrupt object " + id);
		this.id = id;
	}

	public CorruptObjectException(String message) {
		this(null, message);
	}
	private final ObjectId id;

	public ObjectId getObjectId() { return id; }

}
