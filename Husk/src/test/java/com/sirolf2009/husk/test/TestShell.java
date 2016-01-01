package com.sirolf2009.husk.test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.sirolf2009.husk.Command;
import com.sirolf2009.husk.Husk;
import com.sirolf2009.husk.Husk.CommandNotFoundException;
import com.sirolf2009.husk.Husk.CommandSaveException;
import com.sirolf2009.husk.dsl.ParseException;

public class TestShell {

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
	public Class<?> getNumberType(Number number, String... parameters) {
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
	public void test() throws CommandNotFoundException, ParseException, CommandSaveException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		new Husk(this).execute("");
		new Husk(this).execute("testCommand");
		new Husk(this).execute("testCommand hello ");
		new Husk(this).execute("testCommand hello world");
		Assert.assertEquals(Integer.class, new Husk(this).execute("gnt 1").getReturnValue());
		Assert.assertEquals(Long.class, new Husk(this).execute("gnt 1l").getReturnValue());
		Assert.assertEquals(Double.class, new Husk(this).execute("gnt 1.0").getReturnValue());
		Assert.assertEquals(Float.class, new Husk(this).execute("gnt 1.0F").getReturnValue());
		Assert.assertEquals(Float.class, new Husk(this).execute("gnt 1.0f").getReturnValue());
		Assert.assertEquals(Double.class, new Husk(this).execute("gnt 1.0D").getReturnValue());
		Assert.assertEquals(Double.class, new Husk(this).execute("gnt 1.0d").getReturnValue());
		Assert.assertEquals("35394fc9-e96e-4921-95b2-1d4642712d78", new Husk(this).execute("getRandomString").getReturnValue());
		Assert.assertEquals("87d2172464d1-2b59-1294-e69e-9cf49353", new Husk(this).execute("getRandomString | reverse").getReturnValue());
		Assert.assertEquals(36, new Husk(this).execute("getRandomString | reverse | count").getReturnValue());
		Assert.assertTrue(new Husk(this).execute("?list").getReturnValue().toString().contains("Reverse a string"));
	}

}
