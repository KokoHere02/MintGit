package com.mintgit.storage;

import java.io.IOException;
import java.util.Map;

import com.mintgit.core.ObjectId;
import com.mintgit.core.Ref;

public interface RefDatabase {

	Ref resolve(String refName);           // 解析 HEAD、main、refs/heads/main
	Map<String, Ref> getAllRefs() throws IOException;        // 列出所有分支、tag
	void updateRef(String refName, ObjectId target) throws IOException;
	void deleteRef(String refName) throws IOException;

}
