package com.mintGit;

import java.nio.file.Path;

/**
 * Git 仓库对象。
 */
public abstract class Repository {

	private Path gitDir;

	private ObjectDatabase objectDatabase;
}
