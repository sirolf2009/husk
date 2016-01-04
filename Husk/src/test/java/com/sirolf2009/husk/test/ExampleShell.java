package com.sirolf2009.husk.test;

import com.sirolf2009.husk.Command;
import com.sirolf2009.husk.Husk;
import com.sirolf2009.husk.Husk.CommandSaveException;

public class ExampleShell {

	@Command(abbrev="hi", fullName="hello-world", description="A simple greeting")
	public String greet() {
		return "Hello World";
	}

	@Command(description="reverse a string")
	public String reverse(String... stringArray) {
		StringBuffer buffer = new StringBuffer();
		for(String string : stringArray) {
			buffer.append(new StringBuffer(string).reverse().toString()+" ");
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, CommandSaveException {
		new Husk(new ExampleShell(), "My awesome shell name", "My awesome splash!").commandLoop();
	}

}
