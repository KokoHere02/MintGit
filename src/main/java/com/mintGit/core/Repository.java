package com.mintGit.core;

import java.nio.file.Path;

import com.mintGit.storage.ObjectDatabase;

/**
 * Git 仓库对象。
 */
public abstract class Repository {

	private Path gitDir;

	private ObjectDatabase objectDatabase;
}
