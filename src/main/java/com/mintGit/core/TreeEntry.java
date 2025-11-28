package com.mintGit.core;

/**
 * GIT 子树
 * @param mode 文件码
 * @param name 文件名
 * @param id 唯一Id
 */
public record TreeEntry(int mode, String name, ObjectId id) {

	public static final int FILE = 0100644;      // 普通文件
	public static final int EXECUTABLE = 0100755; // 可执行文件
	public static final int DIR = 040000;        // 子树
	public static final int SYMLINK = 0120000;

	public String modeAsString() {
		return String.format("%06o", mode);
	}

}
