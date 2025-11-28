package com.mintgit.exception;

public class InvalidRefException extends MintGitException{

		public InvalidRefException(String refName, String message) {
			super("invalid ref " + refName + ": " + message);
		}

}
