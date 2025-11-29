package com.mintgit.core;

import java.util.Optional;

public record Ref(
	String name,
	ObjectId target,
	boolean symbolic
) {

	public String shortName() {
		if (name.equals("HEAD")) return "HEAD";
		if (name.startsWith("refs/heads/")) return name.substring(11);
		if (name.startsWith("refs/tags/"))  return name.substring(10);
		if (name.startsWith("refs/remotes/")) return name.substring(13);
		return name;
	}

	public boolean isBranch()   { return name.startsWith("refs/heads/"); }
	public boolean isTag()      { return name.startsWith("refs/tags/"); }
	public boolean isRemote()   { return name.startsWith("refs/remotes/"); }

	public Optional<Ref> peeled() {
		return symbolic ? Optional.of(new Ref(target.name(), null, false)) : Optional.empty();
	}

}
