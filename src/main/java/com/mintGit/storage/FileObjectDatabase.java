package com.mintGit.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import com.mintGit.core.GitObject;
import com.mintGit.core.ObjectId;
import com.mintGit.core.StoredObject;

public class FileObjectDatabase implements ObjectDatabase {

	private final Path objectsDir;       // .git/objects
	private final ObjectReader reader = new ObjectReader();


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
		}
		catch (IOException e) {
			throw new UncheckedIOException("写入对象失败: " + obj.id(), e);
		}

	}

	@Override
	public GitObject read(ObjectId id) {
		Path path = loosePath(id);
		if (!Files.exists(path)) {
			throw new MissingFormatArgumentException(id.toString());
		}

		try {
			byte[] compressed = Files.readAllBytes(path);
			byte[] raw = decompress(compressed);
			return reader.read(raw);

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static byte[] decompress(byte[] compressed) {
		Inflater inflater = new Inflater(true);
		inflater.reset();
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

	@Override
	public void writePack(List<ObjectId> ids, OutputStream out, boolean thin) {
		try {
			out.write("PACK".getBytes(StandardCharsets.UTF_8));
			out.write(intToBytes(2));
			out.write(intToBytes(ids.size()));
			MessageDigest packSha1 = MessageDigest.getInstance("SHA-1");
			for (ObjectId id : ids) {
				GitObject obj = read(id);
				byte[] raw = obj.serialize();
				String header = obj.type() + " " + raw.length + "\0";
				byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

				int totalSize = headerBytes.length + raw.length;

				writeVarIntLong(out, (long) typeToCode(obj.type()) << 4 | (totalSize & 0x0F), totalSize >> 4);

				ByteArrayOutputStream temp = new ByteArrayOutputStream();
				try (DeflaterOutputStream dos = new DeflaterOutputStream(temp,
					new Deflater(Deflater.DEFAULT_COMPRESSION,true))) {
					dos.write(headerBytes);
					dos.write(raw);
				}
				out.write(temp.toByteArray());
				packSha1.update(headerBytes);
				packSha1.update(raw);
			}

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private int typeToCode(String type) {
		return switch (type) {
			case "commit" -> 1;
			case "tree" -> 2;
			case "blob" -> 3;
			case "tag" -> 4;
			default -> throw new IllegalArgumentException(type);
		};
	}

	private void writeVarIntLong(OutputStream out, long value, long continuation) {
		try {
			while (true) {
				int b = (int) (value & 0x7F);
				if (continuation != 0) b |= 0x80;
				out.write(b);

				if (continuation == 0) break;
				value = continuation;
				continuation >>>= 7;
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private byte[] intToBytes(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}

	private Path loosePath(ObjectId id) {
		String hex = id.name();
		if (hex.length() != 40) {
			throw new IllegalArgumentException("Invalid ObjectId: " + hex);
		}
		String dir = hex.substring(0, 2);
		String file = hex.substring(2);

		return objectsDir.resolve(dir).resolve(file);
	}


}
