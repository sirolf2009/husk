package com.sirolf2009.husk.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.sirolf2009.husk.Command;
import com.sirolf2009.husk.CommandExecutorDefault;
import com.sirolf2009.husk.Husk;

public class TestDefaultCommandExecutor {

	@Command
	public void testCommand() {
		System.out.println("Executed");
	}
	
	@Command
	public void testCommand(String parameter) {
		System.out.println("Executed with param "+parameter);
	}

	@Command
	public void testCommand(String... parameters) {
		System.out.println("Executed with param "+Arrays.toString(parameters));
	}

	@Command
	public Class<?> getNumberType(Number number, String... strings) {
		return number.getClass();
	}

	@Command
	public int count(String parameter) {
		return parameter.length();
	}
	
	@Command
	public String getRandomString() {
		return "35394fc9-e96e-4921-95b2-1d4642712d78";
	}
	
	@Command(description="Reverse a string")
	public String reverse(String string) {
		return new StringBuilder(string).reverse().toString();
	}
	
	@Test
	public void test() throws Exception {
		Husk husk = new Husk(this);
		CommandExecutorDefault executorDefault = new CommandExecutorDefault(husk);
		executorDefault.execute("");
		executorDefault.execute("testCommand");
		executorDefault.execute("testCommand hello ");
		executorDefault.execute("testCommand hello world");
		Assert.assertEquals(Integer.class, husk.executeForResult("gnt 1").get());
		Assert.assertEquals(Long.class, husk.executeForResult("gnt 1l").get());
		Assert.assertEquals(Double.class, husk.executeForResult("gnt 1.0").get());
		Assert.assertEquals(Float.class, husk.executeForResult("gnt 1.0F").get());
		Assert.assertEquals(Float.class, husk.executeForResult("gnt 1.0f").get());
		Assert.assertEquals(Double.class, husk.executeForResult("gnt 1.0D").get());
		Assert.assertEquals(Double.class, husk.executeForResult("gnt 1.0d").get());
		Assert.assertEquals("35394fc9-e96e-4921-95b2-1d4642712d78", husk.executeForResult("getRandomString").get());
		Assert.assertEquals("87d2172464d1-2b59-1294-e69e-9cf49353", husk.executeForResult("getRandomString | reverse").get());
		Assert.assertEquals(36, husk.executeForResult("getRandomString | reverse | count").get());
		Assert.assertTrue(husk.executeForResult("?list").get().toString().contains("Reverse a string"));
	}

}
