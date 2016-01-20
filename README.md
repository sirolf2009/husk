# Husk
A nutty shell

## Maven
```xml
<dependency>
    <groupId>com.sirolf2009</groupId>
    <artifactId>Husk</artifactId>
    <version>0.0.2</version>
</dependency>
```

## About
This is a java shell to quickly set up commands, rather than building a GUI.
It's intentions are to be able to do anything a standard UNIX shell can do, with the ease of [Cliche Shell](https://code.google.com/p/cliche/), along with some more optional utilities

## Guide
What we'll need is one class where we can add commands and register them to the shell. Let's make it look something like this:

```java
import com.sirolf2009.husk.Command;
import com.sirolf2009.husk.Husk;
import com.sirolf2009.husk.Husk.CommandSaveException;

public class ExampleShell {
	
	@Command
	public String greet() {
		return "Hello World";
	}
	
	@Command
	public String reverse(String string) {
		return new StringBuffer(string).reverse().toString();
	}
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, CommandSaveException {
		new Husk(new ExampleShell(), "My awesome shell name", "My awesome splash!").commandLoop();
	}

}
```
What we've created is a shell that has 2 commands. The command greet and the command reverse. If we run this file, we will see something like this:
```
My awesome splash!
My awesome shell name> 
```
Enter `greet` in your shell and the text `Hello World` will be returned to the terminal.
We have another command, let's type `reverse husk`. The terminal should return `ksuh`. 
But why stop there? what if we want to take the output from `greet` and then `reverse` it? easy.
```
My awesome shell name> greet | reverse
dlroW olleH
```
In the background, `greet` was executed and returned a string. This string was then fed to the reverse command thanks to the `|` character. This is what's known as a pipeline.
With a more advanced setup you could do something like `getNewRecordsFromDatabase | createDatabaseRecordReport | saveToDatabase`. This might not seem easier like writing a function
to do this for you, until you want to do something like this: `getAllRecordsFromDatabase | createDatabaseRecordReport | saveToDatabase`. Now you need to not only write the function
to get all the database records, but also a function to combine the three commands above. Using pipelines, you only need to write the functiong to get everything from your database

Now what if we want to reverse multiple words? Do I have to type the reverse command multiple times? Of course not! Just add varargs to your function
```java
@Command
public String reverse(String... stringArray) {
	StringBuffer buffer = new StringBuffer();
	for(String string : stringArray) {
		buffer.append(new StringBuffer(string).reverse().toString()+" ");
	}
	return buffer.toString();
}
```
Let's test it out!
```
My awesome shell name> reverse Hello World
olleH dlroW 
```

### Abbreviations and names
Isn't it a pain to write word like `greet` and `reverse`? Well good news everyone! For every function, an abbreviation is created. By default these are the camelcase characters.

The word `myAwesomeFunction` would get the abbreviation `maf`. `greet` and `reverse` will get the abbreviation `g` and `r`. This means that instead of doing `greet | reverse`, we can also do `g | r`
If you don't like your default abbreviation, you can always override it, the same thing goes for the full function name. It would be done like this:
```java
@Command(abbrev="hi", fullName="hello-world")
public String greet() {
	return "Hello World";
}
```
The command `greet` is now called `hello-world` and the abbreviation `g` has become `hi`

### The help system
**The help system is still under construction, more features will be added**

If you want to list all commands available to you, you can enter `?list` or `?l`. For the example above, the output would be
```
╔═════════════╤══════════════╤════════════╤══════════════════════════╗
║ Name        │ Abbreviation │ Parameters │ Description              ║
╠═════════════╪══════════════╪════════════╪══════════════════════════╣
║ ?list       │ ?l           │            │ Display all the commands ║
╟─────────────┼──────────────┼────────────┼──────────────────────────╢
║ hello-world │ hi           │            │                          ║
╟─────────────┼──────────────┼────────────┼──────────────────────────╢
║ reverse     │ r            │ String[]   │                          ║
╚═════════════╧══════════════╧════════════╧══════════════════════════╝
```
As you can see, the `?list` command has a description. Don't you want a description for your functions? Of course you do!
```java
@Command(abbrev="hi", fullName="hello-world", description="A simple greeting")
public String greet() {
	return "Hello World";
}

@Command(description="reverse a string array")
public String reverse(String... stringArray) {
	StringBuffer buffer = new StringBuffer();
	for(String string : stringArray) {
		buffer.append(new StringBuffer(string).reverse().toString()+" ");
	}
	return buffer.toString();
}
```
Let's see what we now get when we type `?list`
```
╔═════════════╤══════════════╤════════════╤══════════════════════════╗
║ Name        │ Abbreviation │ Parameters │ Description              ║
╠═════════════╪══════════════╪════════════╪══════════════════════════╣
║ ?list       │ ?l           │            │ Display all the commands ║
╟─────────────┼──────────────┼────────────┼──────────────────────────╢
║ hello-world │ hi           │            │ A simple greeting        ║
╟─────────────┼──────────────┼────────────┼──────────────────────────╢
║ reverse     │ r            │ String[]   │ reverse a string array   ║
╚═════════════╧══════════════╧════════════╧══════════════════════════╝
```

### RMI
RMI stands for Remote Method Invocation and basically means that a client can perform functions on a server. If you enable shared mode on Husk like so:
```java
new Husk(new Handler()).shared(4567);
```
An RMI server will start hosting on port 4567. If you run the same code again, the new instance of husk will search for existing instances of servers. By default it only looks at localhost, but different IP's can be passed as a second parameter.

Once you have your second instance running, you can start executing commands at the server. Say that the server has this handler registered:
```java
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
```
and in the first husk instance, the server instance, you type:
```
gv
sv hello
gv
```
The first get-value will return `null`, makes sense. The second get-value will return `hello`. Again, nothing special here.

Now take your second instance of husk, the client instance, and run `gv`. What do you get? Exactly! `hello`. If you now run `sv world` on the client and `gv` on the server, then the server will display `world`. You can use this to build debugging or monitoring handlers, that can help you with operating servers, or you can treat the shells as multiple shell instances of the same application.
