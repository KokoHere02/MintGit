package com.mintgit.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.Deflater;

import com.mintgit.core.Blob;
import com.mintgit.core.GitObject;
import com.mintgit.core.ObjectId;
import com.mintgit.core.StoredObject;
import com.mintgit.core.Tree;
import com.mintgit.core.TreeEntry;

public class ObjectWriter {

	private final ObjectDatabase db;

	public ObjectWriter(ObjectDatabase db) {
		this.db = db;
	}

	private static final ThreadLocal<Deflater> DEFLATER_CACHE =
		ThreadLocal.withInitial(() -> new Deflater(Deflater.DEFAULT_COMPRESSION, true));

	public ObjectId writeBlob(byte[] data) {
		return write(new Blob(data));
	}

	public ObjectId writeTree(List<TreeEntry> entries) {
		return write(new Tree(entries));
	}

	public ObjectId write(GitObject obj) {
		byte[] raw = obj.serialize();
		System.out.println("[DEBUG] " + obj.type() + " 序列化后原始长度: " + raw.length);

		String header = obj.type() + " " + raw.length + "\0";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(header.getBytes(StandardCharsets.UTF_8));
			out.write(raw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] fullData = out.toByteArray();                     // 先拿到未压缩的完整数据
		System.out.println("[DEBUG] 加 header 后总长度（未压缩）: " + fullData.length);

		ObjectId id = ObjectId.fromBytes(sha1(fullData));        // 对未压缩数据算 ID（正确！）

		byte[] compressed = compress(fullData);                  // 再压缩
		System.out.println("[DEBUG] zlib 压缩后长度: " + compressed.length);

		db.insert(new StoredObject(id, obj.type(), compressed));

		return id;
	}

	private byte[] compress(byte[] data) {
		Deflater deflater = DEFLATER_CACHE.get();
		deflater.reset();
		deflater.setInput(data);
		deflater.finish();

		ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[8192];

		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			baos.write(buffer, 0, count);
		}

		return baos.toByteArray();
	}

	private byte[] sha1(byte[] data) {
		try {

			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(data);
			return sha1.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
