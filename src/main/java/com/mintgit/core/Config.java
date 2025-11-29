package com.mintgit.core;

import java.util.Map;
import java.util.Optional;

public record Config(
	String userName,
	String userEmail,
	Map<String, Remote> remotes,
	Map<String, String> core
) {

	public Config {
		if (userName == null || userName.isBlank()) userName = "Unknow";
		if (userEmail == null || userEmail.isBlank()) userEmail = "unknow@example.com";
		if (remotes == null) remotes = Map.of();
		if (core == null) core = Map.of();
	}

	public Optional<Remote> getRemote(String name) {
		return Optional.ofNullable(remotes.get(name));
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

}
