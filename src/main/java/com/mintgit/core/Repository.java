package com.mintgit.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mintgit.exception.GitRepositoryException;
import com.mintgit.storage.FileObjectDatabase;
import com.mintgit.storage.FileRefDatabase;
import com.mintgit.storage.ObjectDatabase;
import com.mintgit.storage.ObjectReader;
import com.mintgit.storage.ObjectWriter;
import com.mintgit.storage.RefDatabase;

/**
 * Git 仓库对象。
 */
public class Repository implements AutoCloseable{

	// .git目录
	private final Path gitDir;
	private final Path workTree;

	private final ObjectDatabase objects;
	private final RefDatabase refs;
	private final Config config;

	private final ObjectReader objectReader;
	private final ObjectWriter objectWriter;

	public static Builder builder() {
		return new Builder();
	}

	private Repository(Builder builder) {
		this.gitDir = builder.getDir.toAbsolutePath().normalize();
		this.workTree = builder.workTree;

		if (!Files.isDirectory(this.gitDir)) {
			throw new GitRepositoryException("Not a directory: " + this.gitDir);
		}
		this.objects = new FileObjectDatabase(this);
		this.refs = new FileRefDatabase(this);

		this.objectWriter = new ObjectWriter(this);
		this.objectReader = new ObjectReader();
		this.config = new Config(null, null, null, null);
	}

	public Path getGitDir() {return gitDir;}
	public Path getWorkTree() {return workTree;}
	// ======== 核心组件 ========
	public ObjectDatabase getObjectDatabase() { return objects; }
	public RefDatabase    getRefDatabase()    { return refs; }
	public Config         getConfig()         { return config; }
	public ObjectReader   getObjectReader()   { return objectReader; }
	public ObjectWriter   getObjectWriter()   { return objectWriter; }

	public static Repository open(Path path) {
		Path gitDir = findGitDir(path);
		return new Builder().setGitDir(gitDir).build();
	}

	public Ref getHead() {
		return refs.resolve("HEAD");
	}

	public static Path findGitDir(Path start) {
		Path absolutePath = start.toAbsolutePath();
		while (absolutePath != null) {
			Path git = absolutePath.resolve(".git");
			if (Files.isDirectory(git)) {
				return git;
			}
			absolutePath = absolutePath.getParent();
		}
		throw new  GitRepositoryException("Not a directory: " + start);
	}

	public Repository init(String dir) {
		if (dir == null || dir.isEmpty()) {
			dir = System.getProperty("user.dir");
		}

		try {
			Path gitDir = Path.of(dir).resolve(".git");
			Files.createDirectories(gitDir);

			// directories
			Files.createDirectories(gitDir.resolve("hooks"));
			Files.createDirectories(gitDir.resolve("info"));
			Files.createDirectories(gitDir.resolve("objects/info"));
			Files.createDirectories(gitDir.resolve("objects/pack"));
			Files.createDirectories(gitDir.resolve("refs/heads"));
			Files.createDirectories(gitDir.resolve("refs/tags"));

			// files
			Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/master\n");
			Files.writeString(gitDir.resolve("config"),
				"[core]\n\trepositoryformatversion = 0\n\tfilemode = false\n");

			Files.writeString(gitDir.resolve("description"),
				"Unnamed repository; edit this file 'description' to name the repository.\n");

			return Repository.builder().setGitDir(gitDir).setWorkTree(gitDir.getParent()).build();

		} catch (IOException e) {
			throw new GitRepositoryException("Failed to create .git directory: " + dir, e);
		}
	}


	public static class Builder{
		private Path getDir;
		private Path workTree;

		public Builder setGitDir(Path dir){
			this.getDir = dir;
			return this;
		}

		public Builder setWorkTree(Path workTree){
			this.getDir = workTree;
			return this;
		}

		public Repository build(){
			if (getDir == null) throw new GitRepositoryException("Builder Repository error because getDir is null");
			return new Repository(this);
		}
	}

	@Override
	public void close() throws Exception {}

}
