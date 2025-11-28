package com.mintgit.exception;

public class InvalidPackException extends MintGitException {

	public InvalidPackException(String message) {
		super("invalid pack: " + message);
	}

}
