package com.mintgit.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.mintgit.exception.GitRepositoryException;
import com.mintgit.storage.ObjectWriter;

public class GitExecution {

	private final Repository repo;
	private final ObjectWriter writer;

	public  GitExecution(Repository repo,  ObjectWriter writer) {
		this.repo = repo;
		this.writer = writer;
	}

	private void checkGitDir() {
		if (!Files.exists(repo.getGitDir())) {
			throw new GitRepositoryException("Failed to create .git directory: " + repo.getGitDir());
		}
	}

	public void addFile(List<Path> files) {
		checkGitDir();
		for (Path path : files) {
			if (Files.isRegularFile(path)) {
				writer.writeBlob(path);
			}
		}
	}

}
