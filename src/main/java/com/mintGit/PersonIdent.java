package com.mintGit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public record PersonIdent(String name, String email, Instant when, ZoneId zone) {

	public String toExternalString() {
		ZoneOffset offset = zone.getRules().getOffset(when);
		int minutes = offset.getTotalSeconds() / 60;
		String sign = minutes < 0 ? "-" : "+";
		int abs = Math.abs(minutes);
		return "%s <%s> %d %s%02d%02d".formatted(
			name, email,
			when.getEpochSecond(),
			sign, abs / 60, abs % 60
		);
	}

}