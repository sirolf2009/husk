package com.sirolf2009.husk.test;

import org.junit.Assert;
import org.junit.Test;

import com.sirolf2009.husk.Command;
import com.sirolf2009.husk.Husk;

public class TestSharedShell {
	
	@Test
	public void test() throws Exception {
		Husk husk = new Husk(new Handler()).shared(4567);
		Assert.assertEquals(null, husk.executeForResult("gv").orElse(null));
		Assert.assertEquals(null, husk.executeForResult("sv hello").orElse(null));
		Assert.assertEquals("hello", husk.executeForResult("gv").orElse(null));
		
		Husk husk2 = new Husk(new Handler()).shared(4567);
		Assert.assertEquals("hello", husk2.executeForResult("gv").orElse(null));
		Assert.assertEquals(null, husk2.executeForResult("sv world").orElse(null));
		Assert.assertEquals("world", husk2.executeForResult("gv").orElse(null));

		Assert.assertEquals("world", husk.executeForResult("gv").orElse(null));
	}
	
	public static class Handler {
		
		private String value;

		@Command
		public void setValue(String value) {
			this.value = value;
		}
		
		@Command
		public String getValue() {
			return value;
		}
		
	}

}
