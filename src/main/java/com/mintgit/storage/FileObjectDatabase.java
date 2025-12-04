package com.mintgit.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import com.mintgit.core.GitObject;
import com.mintgit.core.ObjectId;
import com.mintgit.core.Repository;
import com.mintgit.core.StoredObject;
import com.mintgit.exception.CorruptObjectException;
import com.mintgit.exception.GitRepositoryException;
import com.mintgit.exception.InvalidPackException;
import com.mintgit.parser.CommitParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileObjectDatabase implements ObjectDatabase {

	private final Path objectsDir;       // .git/objects
	private final Repository repo;
	private final ObjectReader reader = new ObjectReader();
	private static final Logger logger = LoggerFactory.getLogger(FileObjectDatabase.class);


	public FileObjectDatabase(Repository repository) {
		this.objectsDir = repository.getGitDir().resolve("objects");
		this.repo = repository;
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
					logger.error("Data corruption data: {}", obj);
					throw new IllegalStateException(
						"对象已存在但内容不一致！可能数据损坏或 SHA-1 碰撞！ID: " + obj.id()
					);
				}
			}

			Files.write(path, obj.compressed(), StandardOpenOption.CREATE_NEW);
		}
		catch (IOException e) {
			logger.error("write data error id: {}, message: {}", obj.id(), e.getMessage());
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
			logger.error("read file error: {}, message: {}", id, e.getMessage());
			throw new IllegalStateException(e);
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
			logger.error("zlib Unzip error message: {}", e.getMessage());
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
			logger.error("unknow error: {}", e.getMessage());
			throw new InvalidPackException(e.getMessage());
		}
		catch (NoSuchAlgorithmException e) {
			throw new AssertionError("SHA-1 not available on this JVM", e);
		}
	}

	@Override
	public Optional<ObjectId> findByPrefix(String prefix) {
		if (prefix == null ||  prefix.isEmpty()) return Optional.empty();

		prefix = prefix.toLowerCase();
		if (prefix.length() > 40) throw new IllegalArgumentException("prefix too long: " + prefix);

		if (prefix.length() == 40) {
			ObjectId id = ObjectId.fromString(prefix);
			return exists(id) ? Optional.of(id) : Optional.empty();
		}
		String dir = prefix.substring(0, 2);
		Path looseDir = objectsDir.resolve(dir);
		if (Files.isDirectory(looseDir)) {

			String fileName = prefix.substring(2);
			List<ObjectId> candidates = new ArrayList<>();

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(looseDir)) {
				for (Path file : stream) {
					String name = file.getFileName().toString();
					if (name.startsWith(fileName)) {
						ObjectId objectId = ObjectId.fromString(dir + name);
						candidates.add(objectId);
					}
				}
			}
			catch (IOException e) {
				throw new CorruptObjectException("read path: " + looseDir + "Exception. it CorruptObject" +
					" System message"+ e.getMessage());
			}
			if (candidates.size() == 1) {
				return Optional.of(candidates.get(0));
			}

			if (candidates.size() > 1) {
				throw new GitRepositoryException(prefix + "has more than one matching prefix :" + candidates);
			}
		}

		// todo 再查 pack 文件（如果你已经实现 pack 索引）

		return Optional.empty();
	}

	@Override
	public boolean exists(ObjectId id) {
		Path loosePath = loosePath(id);  // 你之前已经写好的方法
		if (Files.isRegularFile(loosePath)) {
			return true;
		}

		// todo 再查 pack 文件（如果你已经实现 pack 索引）
		return false;
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
			throw new InvalidPackException(e.getMessage());
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
