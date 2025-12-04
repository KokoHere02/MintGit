package com.mintgit.core;

public sealed interface GitObject permits Blob, Tree, Commit, Tag {

	ObjectId id();
	String type();
	byte[] serialize();
}
