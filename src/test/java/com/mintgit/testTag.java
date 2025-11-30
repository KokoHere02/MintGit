package com.mintgit;

import java.time.Instant;
import java.time.ZoneId;

import com.mintgit.core.PersonIdent;
import junit.framework.Assert;
import org.junit.Test;

public class testTag {

	@Test
	public void testFormat() {
		var ident = new PersonIdent(
			"张三",
			"zhangsan@github.com",
			Instant.now(),
			ZoneId.of("Asia/Shanghai")
		);
		System.out.println(ident.format());
		Assert.assertNotNull(ident);
	}

	@Test
	public void testParse() {
		String ident = "张三 <zhangsan@github.com> 1764491899 +0800";
		PersonIdent parse = PersonIdent.parse(ident);
		System.out.println(parse);
		System.out.println(parse.format());
		Assert.assertNotNull(parse);
	}

}
