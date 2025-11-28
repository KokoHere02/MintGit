package com.mintGit.storage;

import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.mintGit.core.Blob;
import com.mintGit.core.GitObject;
import com.mintGit.parser.CommitParser;
import com.mintGit.parser.TreeParser;

public class ObjectReader {

	public GitObject read(byte[] rawData) {
		try {
			int nul = findNul(rawData);
			String header = new String(rawData, 0, nul, StandardCharsets.UTF_8);
			String[] parts = header.split(" ");
			String type = parts[0];
			byte[] content = Arrays.copyOfRange(rawData, nul + 1, rawData.length);
			return switch (type) {
				case "blob" -> new Blob(content);
				case "tree" -> TreeParser.parse(content);
				case "commit" -> CommitParser.parse(content);
				default -> throw new IllegalArgumentException("Unknown type " + type);
			};
		} catch (InvalidObjectException e) {
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

	private int findNul(byte[] data) throws InvalidObjectException {
		for(int i = 0; i < data.length; i++) if (data[i] == 0) return i;
		throw new InvalidObjectException("no nul byte");
	}
}
