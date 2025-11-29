package com.mintgit.storage;

import java.io.IOException;
import java.util.Map;

import com.mintgit.core.ObjectId;
import com.mintgit.core.Ref;
import com.mintgit.core.Repository;

public class FileRefDatabase implements RefDatabase {

	public FileRefDatabase(Repository repo) {
	}

	@Override
	public Ref resolve(String refName) {
		return null;
	}

	@Override
	public Map<String, Ref> getAllRefs() throws IOException {
		return Map.of();
	}

	@Override
	public void updateRef(String refName, ObjectId target) throws IOException {

	}

	@Override
	public void deleteRef(String refName) throws IOException {

	}

}
