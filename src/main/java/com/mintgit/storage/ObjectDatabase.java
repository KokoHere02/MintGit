package com.mintgit.storage;

import java.io.OutputStream;
import java.util.List;

import com.mintgit.core.GitObject;
import com.mintgit.core.ObjectId;
import com.mintgit.core.StoredObject;

public interface ObjectDatabase {
	void insert(StoredObject obj);
	GitObject read(ObjectId id);
	void writePack(List<ObjectId> ids, OutputStream out, boolean thin);
}
