package com.mintGit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class FileObjectDatabase implements ObjectDatabase {

	private final Path objectsDir;       // .git/objects

	public FileObjectDatabase(Path objectsDir) {
		this.objectsDir = objectsDir;
	}

	@Override
	public void insert(StoredObject obj) {
		Path path = objectsDir
			.resolve(obj.id().name().substring(0, 2))
			.resolve(obj.id().name().substring(2));

		try {
			Files.createDirectories(path.getParent());
			if (Files.exists(path)) {
				byte[] bytes = Files.readAllBytes(path);
				if (Arrays.equals(bytes,obj.compressed())) return;
				else {
					throw new IllegalStateException(
						"对象已存在但内容不一致！可能数据损坏或 SHA-1 碰撞！ID: " + obj.id()
					);
				}
			}

			Files.write(path, obj.compressed(), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			throw new UncheckedIOException("写入对象失败: " + obj.id(), e);
		}

	}

	@Override
	public GitObject read(ObjectId id) {
		return null;
	}

	@Override
	public void writePack(List<ObjectId> ids, OutputStream out, boolean thin) {

	}

//	private ObjectId computeObjectId(GitObject obj) {
//
//	}

}
