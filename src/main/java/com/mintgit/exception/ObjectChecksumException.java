package com.mintgit.exception;

import com.mintgit.core.ObjectId;

public class ObjectChecksumException extends  MintGitException {

	private final ObjectId expected;
	private final ObjectId actual;

	public ObjectChecksumException(ObjectId expected, ObjectId actual) {
		super(String.format("SHA-1 checksum mismatch: expected %s, got %s", expected, actual));
		this.expected = expected;
		this.actual   = actual;
	}

	public ObjectChecksumException(ObjectId id, String detail) {
		super("SHA-1 checksum mismatch for object " + id + ": " + detail);
		this.expected = id;
		this.actual   = null;
	}

	public ObjectId getExpected() { return expected; }
	public ObjectId getActual()   { return actual;   }

}
