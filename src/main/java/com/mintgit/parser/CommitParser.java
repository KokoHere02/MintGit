package com.mintgit.parser;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mintgit.core.Commit;
import com.mintgit.core.ObjectId;
import com.mintgit.core.PersonIdent;
import com.mintgit.exception.InvalidObjectIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitParser {

	private static final Logger logger = LoggerFactory.getLogger(CommitParser.class);

	private static final DateTimeFormatter GIT_DATE_FORMAT = DateTimeFormatter.ofPattern(
		"EEE MMM ppd HH:mm:ss yyyy Z", Locale.ENGLISH);

	public static Commit parse(byte[] raw) {
		String text = new String(raw, StandardCharsets.UTF_8);
		return parse(text);
	}

	public static Commit parse(String text) {
		String[] lines = text.split("\n");
		ObjectId tree = null;
		List<ObjectId> parents = new ArrayList<>();
		PersonIdent author = null;
		PersonIdent committer = null;
		String message = null;
		int i = 0;

		while (i < lines.length) {
			String line = lines[i];
			if (line.isBlank()) {
				i++;
				continue;
			}
			int space = line.indexOf(' ');
			if (space <= 0) throw new IllegalArgumentException("Invalid commit line " + line);

			String key = line.substring(0, space);
			String value = line.substring(space + 1);

			switch (key) {
				case "tree" -> tree = new ObjectId(value);
				case "parent" -> parents.add(new ObjectId(value));
				case "author" -> author = parsePerson(value);
			}
			i++;
		}

		StringBuilder msg = new StringBuilder();
//		while (i < lines.length) {
//			msg.append(lines[i]);
//			if (i < lines.length - 1) msg.append('\n');
//			i++;
//		}
		message = msg.toString();

		if (tree == null) throw new InvalidObjectIdException("commit missing tree");
		if (author == null) throw new InvalidObjectIdException("commit missing author");
		if (committer == null) throw new InvalidObjectIdException("commit missing committer");

		return new Commit(tree, List.copyOf(parents), author, committer, message);
	}

	private static PersonIdent parsePerson(String s) {
		// 格式：name <email> epoch timezone
		// 示例：Alice <alice@example.com> 1737830400 +0800

		int lt = s.lastIndexOf('<');
		int gt = s.indexOf('>', lt + 1);
		int timeStart = s.lastIndexOf(' ',gt + 2);

		if (lt == -1 || gt == -1 || timeStart == -1) {
			logger.error("email Incorrect format :{}", s);
			throw new IllegalArgumentException("Invalid person string: " + s);
		}

		String name = s.substring(0, lt - 1).trim();
		String email = s.substring(lt + 1, gt);
		String timePart = s.substring(timeStart + 1);

		String[] tp = timePart.split(" ");
		if (tp.length != 2) throw new IllegalArgumentException("Invalid time: " + s);

		long epochSecond = Long.parseLong(tp[0]);
		String zoneOffset = tp[1];

		int offsetHours = Integer.parseInt(zoneOffset.substring(1, 3));
		int offsetMinutes = Integer.parseInt(zoneOffset.substring(3));
		if (zoneOffset.charAt(0) == '-') {
			offsetHours = -offsetHours;
			offsetMinutes = -offsetMinutes;
		}
		ZoneId zoneId = ZoneId.ofOffset("UTC", ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes));
		Instant when = Instant.ofEpochSecond(epochSecond);

		return new PersonIdent(name, email, when, zoneId);
	}

}
