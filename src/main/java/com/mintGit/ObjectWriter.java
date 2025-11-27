package com.mintGit;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ObjectWriter {

	private final ObjectDatabase db;

	public ObjectWriter(ObjectDatabase db) throws NoSuchAlgorithmException {
		this.db = db;
	}

	private static final ThreadLocal<Deflater> DEFLATER_CACHE =
		ThreadLocal.withInitial(() -> new Deflater(Deflater.DEFAULT_COMPRESSION, true));

	public ObjectId writeBlob(byte[] data) throws NoSuchAlgorithmException {
		return write(new Blob(data));
	}

	public ObjectId writeTree(List<TreeEntry> entries) {
		return write(new Tree(entries));
//		List<TreeEntry> sorted = new ArrayList<>(entries);
//		sorted.sort(Comparator.comparing(TreeEntry::name));
//
//		ByteArrayOutputStream body = new ByteArrayOutputStream();
//		for (TreeEntry entry : sorted) {
//			body.write(entry.modeAsString().getBytes(StandardCharsets.UTF_8));
//			body.write(' ');
//
//			body.write(entry.name().getBytes(StandardCharsets.UTF_8));
//			body.write(0);
//
//			body.write(entry.id().getBytes());
//		}
//
//		byte[] rawData = body.toByteArray();
//		ByteArrayOutputStream header = new ByteArrayOutputStream();
//		String headerTreeStr = "tree " + rawData.length + "\0";
//		header.write(headerTreeStr.getBytes(StandardCharsets.UTF_8));
//		header.write(rawData);
//
//		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
//		sha1.update(header.toByteArray());
//		byte[] digest = sha1.digest();

	}

	public ObjectId write(GitObject obj) {
		byte[] raw = obj.serialize();
		String header = obj.type() + " " + raw.length + "\0";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.writeBytes(header.getBytes(StandardCharsets.UTF_8));
		out.writeBytes(raw);

		// zlib压缩
		byte[] compressed = compress(out.toByteArray());

		ObjectId id = ObjectId.fromBytes(sha1(out.toByteArray()));

		db.insert(new StoredObject(id,obj.type(),compressed));

		return id;
	}

	private byte[] compress(byte[] data) {
		Deflater deflater = DEFLATER_CACHE.get();
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

	private static byte[] decompress(byte[] compressed) {
		Inflater inflater = new Inflater(true);
		try {
			inflater.setInput(compressed);
			ByteArrayOutputStream boas = new ByteArrayOutputStream(compressed.length + 2);
			byte[] bytes = new byte[8192];
			while (!inflater.finished()) {
				int count = inflater.inflate(bytes);
				if (count == 0 && inflater.needsInput()) {
					break;
				}
				boas.write(bytes, 0, count);
			}
			return boas.toByteArray();

		}
		catch (DataFormatException e) {
			throw new IllegalStateException("zlib 解压失败，对象可能损坏", e);
		}
		finally {
			inflater.end();
		}

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
