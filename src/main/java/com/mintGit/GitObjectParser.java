package com.mintGit;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class GitObjectParser {

//	public static GitObject parse(ObjectId expectedId, byte[] raw) {
//		int nul = indexOf(raw, new byte[] {(byte) 0});
//		String header = new String(raw, 0, nul, StandardCharsets.UTF_8);
//		String type = header.split(" ")[0];
//		byte[] content = Arrays.copyOfRange(raw, nul + 1, raw.length);
//
//		return switch (type) {
//			case "blob"    -> new Blob(content);
//			case "tree"    -> new Tree(TreeParser.parse(content));
//			case "commit"  -> CommitParser.parse(content);
//			default        -> throw new IllegalArgumentException("未知类型: " + type);
//		};
//	}

}
