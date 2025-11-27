package com.mintGit;

public record StoredObject(
	ObjectId id,
	String type,
	byte[] compressed
) {}
