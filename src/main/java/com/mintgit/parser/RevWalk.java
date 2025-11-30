package com.mintgit.parser;

import com.mintgit.core.Commit;
import com.mintgit.core.GitObject;
import com.mintgit.core.ObjectId;
import com.mintgit.core.Ref;
import com.mintgit.core.Repository;
import com.mintgit.core.Tag;
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
			return repo.getObjectDatabase().findByPrefix(rev)
				.orElseThrow(() -> new GitRepositoryException("object not found"));
		}

		Ref ref = repo.getRefDatabase().resolve(rev);
		if (ref != null) return followSymbolic(ref);

		if (rev.contains("~") || rev.contains("^")) {
			parseAncestor(rev);
		}

		throw new GitRepositoryException("unknown revision: " + rev);

	}

	private ObjectId parseAncestor(String rev) {
		String[] parts = rev.split("~");
		String base = parts[0];
		int steps = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

		ObjectId start = parse(base);
		Commit commit = (Commit)repo.getObjectDatabase().read(start);

		for (int i = 0; i < steps; i++) {
			if (commit.parents().isEmpty()) return ObjectId.zeroId();
			commit = (Commit) repo.getObjectDatabase().read(commit.parents().get(0));
		}
		return commit.id();
	}

	private ObjectId followSymbolic(Ref ref) {
		ObjectId target = ref.target();
		GitObject gitObject = repo.getObjectDatabase().read(target);
		if (gitObject instanceof Tag tag) {
			return tag.objectId();
		}
		return target;
	}

}
