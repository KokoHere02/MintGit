package com.mintgit.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mintgit.exception.StorageException;

/**
 * Git 对象
 */
public record Tree(List<TreeEntry> entries) implements GitObject {
	@Override
	public ObjectId id() {
		return null;
	}

	@Override
	public String type() {
		return "tree";
	}

	@Override
	public byte[] serialize() {
		List<TreeEntry> sorted = new ArrayList<>(entries);
		sorted.sort(Comparator.comparing(TreeEntry::name));

		try {
			ByteArrayOutputStream body = new ByteArrayOutputStream();
			for (TreeEntry entry : sorted) {
				body.write(entry.modeAsString().getBytes(StandardCharsets.UTF_8));
				body.write(' ');

				body.write(entry.name().getBytes(StandardCharsets.UTF_8));
				body.write(0);

				body.write(entry.id().getBytes());
			}
			return body.toByteArray();
		} catch (IOException e) {
			throw new StorageException("TreeEntry serialize Exception", e);
		}

	}

}
