package com.mintGit.core;

public sealed interface GitObject permits Blob, Tree, Commit {

	ObjectId id();
	String type();
	byte[] serialize();
}
