package com.mintgit.core;

import com.mintgit.exception.GitRepositoryException;

public record Remote(
	String name,
	String url,
	String fetchRefSpec,
	String pushRefSpec
) {
	public Remote {
		if (name == null || name.isBlank()) throw new GitRepositoryException("remote name required");
		if (url == null || url.isBlank()) throw new GitRepositoryException("remote url required");
	}

}
