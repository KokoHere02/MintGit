package com.mintgit.exception;

import com.mintgit.core.ObjectId;

public class MissingObjectException extends MintGitException {

	private final ObjectId id;
	private final  String typeHint;

	public MissingObjectException(ObjectId id) {
		this(id,null);
	}

	public MissingObjectException(ObjectId id, String typeHint) {
		super("missing " + (typeHint != null ? typeHint + " " : "") + id);
		this.id = id;
		this.typeHint = typeHint;
	}

	public ObjectId getObjectId() { return id; }

}
