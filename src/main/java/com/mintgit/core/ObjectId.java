package com.mintgit.core;

import java.util.Objects;

/**
 * sha1加密后的唯一Id
 */
public record ObjectId(String name) implements Comparable<ObjectId> {

	public ObjectId {
		Objects.requireNonNull(name, "name must not be null");
		if (name.length() != 40) throw new IllegalArgumentException("必须40位");
		if (!name.matches("[0-9a-f]{40}")) throw new IllegalArgumentException("必须小写十六进制");
	}

	/**
	 * 从Sha1Byte提取前20个字节
	 * @param sha1Bytes sha1加密字节数组
	 * @return ObjectId
	 */
	public static ObjectId fromBytes(byte[] sha1Bytes) {
		if (sha1Bytes == null || sha1Bytes.length < 20) {
			throw new IllegalArgumentException("SHA-1 必须是 20 字节");
		}

		StringBuilder sb = new StringBuilder(20);
		for (byte b : sha1Bytes) {
			sb.append(String.format("%02x", b & 0xFF));
		}

		return new ObjectId(sb.toString());
	}

	/**
	 * 从name中提取20为字节
	 * @return 提取后的字节数组
	 */
	public byte[] getBytes() {
		byte[] bytes = new byte[20];
		for (int i = 0; i < 20; i++) {
			bytes[i] = (byte) ((Character.digit(name.charAt(i * 2), 16) << 4) |
				Character.digit(name.charAt(i * 2 + 1), 16));
		}
		return bytes;
	}

	public boolean startsWith(String prefix) {
		return name.startsWith(prefix);
	}

	@Override
	public int compareTo(ObjectId o) {
		return 0;
	}

}
