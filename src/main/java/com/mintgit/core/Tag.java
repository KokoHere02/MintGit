package com.mintgit.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import com.mintgit.exception.GitRepositoryException;
import com.mintgit.storage.ObjectWriter;

public record Tag(
	ObjectId objectId,
	String objectType,
	String tagName,
	PersonIdent tagger,
	String message
) implements GitObject{

	public Tag{
		Objects.requireNonNull(objectId, "objectId cannot be null");
		Objects.requireNonNull(objectType, "objectType cannot be null");
		Objects.requireNonNull(tagName, "tagName cannot be null");
		Objects.requireNonNull(tagger, "tagger cannot be null");
		if (message == null) message = "";  // 允许空消息
	}

	@Override
	public ObjectId id() {
		byte[] tagSer = serialize();
		byte[] bytes = ObjectWriter.withHeader(type(), tagSer);
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new GitRepositoryException("MessageDigest SHA-1 not supported", e);
		}
		byte[] digest = md.digest(bytes);
		return ObjectId.fromBytes(digest);
	}

	@Override
	public String type() {
		return "tag";
	}

	@Override
	public byte[] serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("object ").append(objectId.name()).append('\n');
		sb.append("type ").append(objectType).append('\n');
		sb.append("tag ").append(tagName).append('\n');
		sb.append("tagger ").append(tagger.format()).append('\n');
		if (!message.isBlank()) {
			sb.append('\n');  // 空行分隔 header 和 body
			sb.append(message.stripTrailing());
			if (!message.endsWith("\n")) sb.append('\n');
		}

		byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
		return ObjectWriter.withHeader("tag", content);
	}

}
