package com.mintgit.exception;

public class InvalidObjectIdException extends MintGitException {
	private final String invalid;

	public InvalidObjectIdException(String invalid) {
		super("invalid object id: " + invalid + " (must be 40 hex digits)");
		this.invalid = invalid;
	}

	public InvalidObjectIdException(String invalid, Throwable cause) {
		super("invalid object id: " + invalid, cause);
		this.invalid = invalid;
	}

	public String getInvalidId() { return invalid; }
}
