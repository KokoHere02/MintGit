package com.mintgit.core;

import java.nio.charset.StandardCharsets;
import java.util.List;

public record Commit(
	ObjectId tree,
	List<ObjectId> parents,
	PersonIdent author,
	PersonIdent committer,
	String message
) implements GitObject {

	@Override
	public ObjectId id() {
		return null;
	}

	@Override
	public String type() {
		return "commit";
	}

	@Override
	public byte[] serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("tree ").append(tree).append("\n");

		for (ObjectId p : parents) {
			sb.append("parent ").append(p).append('\n');
		}

		sb.append("author ").append(author.toExternalString()).append("\n");
		sb.append("committer ").append(committer.toExternalString()).append("\n");

		sb.append('\n');
		sb.append(message);
		if (!message.endsWith("\n")) sb.append("\n");
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

}
