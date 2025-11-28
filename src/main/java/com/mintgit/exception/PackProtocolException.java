package com.mintgit.exception;

public class PackProtocolException extends RuntimeException{

	public PackProtocolException(String message) {
		super("pack protocol error: " + message);
	}

}
