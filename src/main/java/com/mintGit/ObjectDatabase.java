package com.mintGit;

import java.io.OutputStream;
import java.util.List;

public interface ObjectDatabase {
	void insert(StoredObject obj);
	GitObject read(ObjectId id);
	void writePack(List<ObjectId> ids, OutputStream out, boolean thin);
}
