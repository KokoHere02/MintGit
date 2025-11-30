package com.mintgit.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

public record PersonIdent(String name, String email, Instant when, ZoneId zone) {

	public String format() {
		ZoneOffset offset = zone.getRules().getOffset(when);
		int totalMinutes = offset.getTotalSeconds() / 60;

		String tz;
		if (totalMinutes == 0) {
			tz = "Z";
		}
		else {
			String sign = totalMinutes < 0 ? "-" : "+";
			int abs = Math.abs(totalMinutes);
			tz = "%s%02d%02d".formatted(sign, abs / 60, abs % 60);
		}

		return "%s <%s> %d %s".formatted(name, email, when.getEpochSecond(), zone);
	}

	public static PersonIdent parse(String line) {
		// "Alice <alice@example.com> 1739250000 +0800"
		var matcher = Pattern.compile("(.*) <(.*?)> (\\\\d+) ([-+Z]\\\\S+)").matcher(line);

		String name = matcher.group(1);
		String email = matcher.group(2);
		Instant when = Instant.ofEpochSecond(Long.parseLong(matcher.group(3)));
		String tz = matcher.group(4);

		ZoneId zone = tz.equals("Z")
			? ZoneOffset.UTC
			: ZoneId.ofOffset("", ZoneOffset.of(tz));

		return new PersonIdent(name, email, when, zone);
	}

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