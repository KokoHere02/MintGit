package com.mintgit.core;

public record StoredObject(
	ObjectId id,
	String type,
	byte[] compressed
) {}
