package com.mintGit;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Test;

public class testTree {

	@Test
	public void testTree() throws NoSuchAlgorithmException {
		String src = "D:\\java\\code\\test\\.git\\objects";
		Path path = Path.of(src);
		FileObjectDatabase database = new FileObjectDatabase(path);
		ObjectWriter writer = new ObjectWriter(database);

		// 2. 创建两个 blob（模拟两个文件）
		ObjectId blob1 = writer.write(new Blob("test content\n".getBytes()));
		ObjectId blob2 = writer.write(new Blob("public class Main {}\n".getBytes()));

		System.out.println("Blob1 ID: " + blob1);
		System.out.println("Blob2 ID: " + blob2);

	}

}
