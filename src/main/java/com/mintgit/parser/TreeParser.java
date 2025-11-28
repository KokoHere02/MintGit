package com.mintgit.parser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mintgit.core.ObjectId;
import com.mintgit.core.Tree;
import com.mintgit.core.TreeEntry;
import com.mintgit.exception.CorruptObjectException;

public class TreeParser {

	public static Tree parse(byte[] raw) {
		List<TreeEntry> entries = new ArrayList<>();
		int pos = 0;
		while (pos < raw.length) {
			int space = indexOf(raw, pos, (byte)' ');
			int nul = indexOf(raw, space, (byte)0);

			String modeStr = new String(raw, pos, space - pos, StandardCharsets.UTF_8);
			int mode = Integer.parseInt(modeStr, 8);
			String name = new String(raw, space + 1, nul - space - 1, StandardCharsets.UTF_8);
			byte[] idBytes = Arrays.copyOfRange(raw, nul + 1, nul + 21);
			ObjectId id = ObjectId.fromBytes(idBytes);

			entries.add(new TreeEntry(mode, name, id));
			pos = nul + 21;
		}
		return new Tree(entries);
	}

	private static int indexOf(byte[] data, int from, byte b) {
		for (int i = from; i < data.length; i++) {
			if (data[i] == b) return i;
		}
		throw new CorruptObjectException("invalid tree format");
	}

}
