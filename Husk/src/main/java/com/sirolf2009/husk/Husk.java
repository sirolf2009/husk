package com.sirolf2009.husk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sirolf2009.husk.CommandExecutorDefault.CommandNotFoundException;
import com.sirolf2009.husk.CommandRegister.CommandSaveException;
import com.sirolf2009.husk.shared.server.HuskServer;

public class Husk {

	private Object handler;
	private String name;
	private String splash;
	private BuiltinHandler builtinHandler;
	private CommandRegister commandRegister;
	private CommandExecutor commandExecutor;
	private List<InputConverter> inputConverters;
	private List<OutputConverter> outputConverters;
	private HuskServer server;
	private static final String defaultSplash = ""+
			" ____  ____                 __       \n"+
			"|_   ||   _|               [  |  _\n"+   
			"  | |__| |  __   _   .--.   | | / ]\n"+  
			"  |  __  | [  | | | ( (`\\]  | '' <\n"+   
			" _| |  | |_ | \\_/ |, `'.'.  | |`\\ \\\n"+  
			"|____||____|'.__.'_/[\\__) )[__|  \\_]\tA nutty shell\n"+ 
			"                                     ";

	public Husk(Object handler) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		this(handler, "Husk");
	}

	public Husk(Object handler, String name) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		this(handler, name, defaultSplash);
	}

	public Husk(Object handler, String name, String splash) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		this(handler, name, splash, new BuiltinHandler());
	}

	public Husk(Object handler, String name, String splash, BuiltinHandler builtinHandler) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		this.handler = handler;
		this.name = name;
		this.splash = splash;
		this.builtinHandler = builtinHandler;
		commandRegister = new CommandRegister();
		commandRegister.registerMethodHandler(this, builtinHandler);
		commandRegister.registerMethodHandler(this, handler);
		inputConverters = new ArrayList<InputConverter>();
		outputConverters = new ArrayList<OutputConverter>();
		commandExecutor = new CommandExecutorDefault(this);
	}
	
	public void commandLoop() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		if(splash != null && !splash.isEmpty()) {
			System.out.println(splash);
		}
		while(true) {
			try {
				System.out.print(name+"> ");
				execute(reader.readLine());
			} catch (CommandNotFoundException e) {
				System.err.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Husk shared(int port) {
		return shared(port, "localhost");
	}
	
	public Husk shared(int port, String... IPs) {
		commandExecutor = new CommandExecutorShared(this, port, IPs);
		return this;
	}
	
	public void execute(String command) throws Exception {
		commandExecutor.execute(command);
	}
	
	public Optional<Object> executeForResult(String command) throws Exception {
		return commandExecutor.executeForResult(command);
	}
	
	public Optional<String> convert(Object object) {
		for(OutputConverter outputConverter : outputConverters) {
			String returned = outputConverter.convert(object);
			if(returned != null) {
				return Optional.of(returned);
			}
		}
		return Optional.empty();
	}
	
	public String getSplash() {
		return splash;
	}

	public void setSplash(String splash) {
		this.splash = splash;
	}

	public List<InputConverter> getInputConverters() {
		return inputConverters;
	}

	public void setInputConverters(List<InputConverter> inputConverters) {
		this.inputConverters = inputConverters;
	}

	public List<OutputConverter> getOutputConverters() {
		return outputConverters;
	}

	public void setOutputConverters(List<OutputConverter> outputConverters) {
		this.outputConverters = outputConverters;
	}

	public Object getHandler() {
		return handler;
	}

	public void setHandler(Object handler) {
		this.handler = handler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BuiltinHandler getBuiltinHandler() {
		return builtinHandler;
	}

	public void setBuiltinHandler(BuiltinHandler builtinHandler) {
		this.builtinHandler = builtinHandler;
	}
	
	public CommandRegister getCommandRegister() {
		return commandRegister;
	}

	public void setCommandRegister(CommandRegister commandRegister) {
		this.commandRegister = commandRegister;
	}

	public HuskServer getServer() {
		return server;
	}

	public void setServer(HuskServer server) {
		this.server = server;
	}

}
