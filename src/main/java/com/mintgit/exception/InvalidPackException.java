package com.mintgit.exception;

public class InvalidPackException extends RuntimeException {

	public InvalidPackException(String message) {
		super("invalid pack: " + message);
	}

}
