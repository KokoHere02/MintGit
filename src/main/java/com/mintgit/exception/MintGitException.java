package com.mintgit.exception;

abstract class MintGitException extends RuntimeException{

	protected MintGitException(String message) { super(message); }
	protected MintGitException(String message, Throwable cause) { super(message, cause); }
}
