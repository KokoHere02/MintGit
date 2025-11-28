package com.mintGit.core;

public record StoredObject(
	ObjectId id,
	String type,
	byte[] compressed
) {}
