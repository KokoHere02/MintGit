package com.mintGit.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Git 数据对象。
 */
public record Blob(byte[] data) implements GitObject {

	@Override
	public ObjectId id() {
		return computeId(data);
	}

	@Override
	public String type() {
		return "blob";
	}

	@Override
	public byte[] serialize() {
		return data;
	}

	/**
	 * 计算Sha1
	 * @param content 数据
	 * @return ObjectId
	 */
	private static ObjectId computeId(byte[] content) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

			String header = "blob " + content.length + "\0";
			sha1.update(header.getBytes(StandardCharsets.UTF_8));

			sha1.update(content);

			return ObjectId.fromBytes(sha1.digest());
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
