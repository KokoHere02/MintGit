package com.mintgit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.mintgit.core.Blob;
import com.mintgit.core.GitObject;
import com.mintgit.core.ObjectId;
import com.mintgit.core.Tree;
import com.mintgit.core.TreeEntry;
import com.mintgit.storage.FileObjectDatabase;
import com.mintgit.storage.ObjectDatabase;
import com.mintgit.storage.ObjectWriter;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class testTree {

	@Test
	public void testblob() throws NoSuchAlgorithmException {
		String src = "D:\\java\\code\\test\\.git\\objects";
		Path path = Path.of(src);
		FileObjectDatabase database = new FileObjectDatabase(path);
		ObjectWriter writer = new ObjectWriter(database);

		// 2. 创建两个 blob（模拟两个文件）
		ObjectId blob1 = writer.write(new Blob("test content\n".getBytes()));

		System.out.println("Blob1 ID: " + blob1);
		Assert.assertNotNull(blob1);
	}

	@Test
	public void testTree() throws NoSuchAlgorithmException, IOException {
		Path gitDir = Path.of("D:/mintGit-test-repo");
		Path objectsDir = gitDir.resolve(".git/objects");

		FileObjectDatabase database = new FileObjectDatabase(objectsDir);
		ObjectWriter writer = new ObjectWriter(database);

		ObjectId blobId = writer.write(new Blob("Hello mintGit\n".getBytes()));
		System.out.println("Blob1 ID: " + blobId);

		Tree tree = new Tree(List.of(
			new TreeEntry(0100644, "test.txt", blobId)  // 必须有第三个参数：blob 的 ObjectId
		));

		// 4. 写入 tree 并获取 ID
		ObjectId treeId = writer.write(tree);
		System.out.println("Tree ID: " + treeId);

		// 5. 断言：文件真的写进去了
		Path treeFilePath = objectsDir
			.resolve(treeId.name().substring(0, 2))
			.resolve(treeId.name().substring(2));
		System.out.println("Tree File Size: " + Files.size(treeFilePath));
		assertTrue("Tree 对象必须写入磁盘", Files.exists(treeFilePath) );
		// 改成这样（正确做法）：
		assertTrue(Files.size(treeFilePath) > 10); // 压缩后最小也就 12~15 字节
	}

	@Test
	public void testReadBlob() throws IOException {
		Path gitDir = Path.of("D:/mintGit-test-repo");
		Path objectsDir = gitDir.resolve(".git/objects");
		Files.createDirectories(objectsDir);

		ObjectDatabase database = new FileObjectDatabase(objectsDir);
		ObjectWriter writer = new ObjectWriter(database);

		// write
		String Content = "Hello mintGit!\n我是手搓 Git \n";
		Blob blob = new Blob(Content.getBytes(StandardCharsets.UTF_8));
		ObjectId objectId = writer.write(blob);
		System.out.println("Blob ID: " + objectId);
		System.out.println(objectsDir.toAbsolutePath());

		// read
		GitObject gitObject = database.read(objectId);
		Blob readBlob = (Blob) gitObject;
		String rContent = new String(readBlob.data(), StandardCharsets.UTF_8);
		System.out.println("Blob Content: " + rContent);
		Assert.assertNotNull(gitObject);
	}


	@Test
	public void testReadTree() throws IOException {
		Path gitDir = Path.of("D:/mintGit-test-repo");
		Path objectsDir = gitDir.resolve(".git/objects");
		Files.createDirectories(objectsDir);

		ObjectDatabase database = new FileObjectDatabase(objectsDir);
		ObjectWriter writer = new ObjectWriter(database);

		// write
		String Content = "Hello mintGit!\n我是手搓 Git \n";
		Blob blob = new Blob(Content.getBytes(StandardCharsets.UTF_8));
		ObjectId objectId = writer.write(blob);
		ObjectId writeTreeId = writer.writeTree(List.of(new TreeEntry(0100644, "test.txt", objectId)));
		System.out.println("Blob ID: " + objectId);
		System.out.println("Tree ID: " + writeTreeId);
		System.out.println(objectsDir.toAbsolutePath());

		// read
		GitObject gitObject = database.read(writeTreeId);

		Tree readTree = (Tree) gitObject;
		assertEquals(1, readTree.entries().size());
		// 验证第一个 entry
		TreeEntry e1 = readTree.entries().get(0);
		assertEquals("test.txt", e1.name());
		assertEquals(0100644, e1.mode());
		assertEquals(blob.id(), e1.id());
		System.out.println("TreeEntry ID: " + e1.id());

		Assert.assertNotNull(gitObject);
	}

}
