package com.mintgit.core;

import java.nio.file.Path;

import com.mintgit.storage.ObjectDatabase;

/**
 * Git 仓库对象。
 */
public abstract class Repository {

	private Path gitDir;

	private ObjectDatabase objectDatabase;
}
