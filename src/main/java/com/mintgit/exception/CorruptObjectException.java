package com.mintgit.exception;

import com.mintgit.core.ObjectId;

public class CorruptObjectException extends MintGitException {
	private final ObjectId id;

	public CorruptObjectException(ObjectId id, String message) {
		super("corrupt object " + id + "："+ message);
		this.id = id;
	}

	public CorruptObjectException(ObjectId id) {
		super("corrupt object " + id);
		this.id = id;
	}

}
