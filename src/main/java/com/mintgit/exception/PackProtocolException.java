package com.mintgit.exception;

public class PackProtocolException extends MintGitException{

	public PackProtocolException(String message) {
		super("pack protocol error: " + message);
	}

}
