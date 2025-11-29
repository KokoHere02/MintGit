package com.mintgit.parser;

import com.mintgit.core.ObjectId;
import com.mintgit.core.Repository;
import com.mintgit.exception.GitRepositoryException;

public class RevWalk implements AutoCloseable{
	@Override
	public void close() throws Exception {
	}

	private final Repository repo;

	public RevWalk(Repository repo) {
		this.repo = repo;
	}

	public ObjectId parse(String rev) {
		if (rev == null || rev.isBlank()) throw new GitRepositoryException("revision cannot be empty");

		if (ObjectId.isValidHex(rev)) {
			return ObjectId.fromString(rev);
		}

		if (rev.matches("[a-f0-9]{4,39}")) {
			ObjectId id = ObjectId.fromString(rev);
			return repo.getObjectDatabase().findByPrefix(rev).orElseThrow(() -> new GitRepositoryException("object not found"));
		}
		return null;

	}

}
