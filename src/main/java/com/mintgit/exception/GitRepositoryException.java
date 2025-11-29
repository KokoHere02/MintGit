package com.mintgit.exception;

public class GitRepositoryException extends MintGitException{

	public GitRepositoryException(String message) {
		super(message);
	}

	public GitRepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
