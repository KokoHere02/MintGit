package com.mintgit.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class RawParseUtils {

	private static final byte[] digits16;

	private RawParseUtils() {
	}

	static {
		digits16 = new byte['f' + 1];
		Arrays.fill(digits16, (byte) -1);
		for (char i = '0'; i <= '9'; i++)
			digits16[i] = (byte) (i - '0');
		for (char i = 'a'; i <= 'f'; i++)
			digits16[i] = (byte) ((i - 'a') + 10);
		for (char i = 'A'; i <= 'F'; i++)
			digits16[i] = (byte) ((i - 'A') + 10);
	}

	public static int parseHexInt4(byte b) {
		int v = b & 0xFF;
		if (v > 'f') return -1;
		int r = digits16[v];
		if (r < 0) throw new IllegalArgumentException("Invalid hex char: " + (char)b);
		return r;
	}

	public static byte[] parseObjectId(String hex) {
		if (hex.length() != 40) {
			throw new IllegalArgumentException("ObjectId must be 40 hex characters, got " + hex.length());
		}

		byte[] bytes = new byte[20];
		for (int i = 0; i < 40; i += 2) {
			int hi = parseHexInt4((byte) hex.charAt(i));
			int lo = parseHexInt4((byte) hex.charAt(i + 1));
			bytes[i / 2] = (byte) ((hi << 4) | lo);
		}
		return bytes;
	}

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static String encodeHex(byte[] data) {
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			int b = data[i] & 0xFF;
			chars[i * 2]     = HEX_CHARS[b >>> 4];
			chars[i * 2 + 1] = HEX_CHARS[b & 0x0F];
		}
		return new String(chars);
	}

	public static String decodeUTF8(byte[] data, int offset, int length) {
		return new String(data, offset, length, StandardCharsets.UTF_8);
	}

	public static int nextLF(byte[] data, int offset) {
		while (offset < data.length && data[offset] != '\n') offset++;
		return offset < data.length ? offset + 1 : data.length;
	}


}
