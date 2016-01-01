package com.sirolf2009.husk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sirolf2009.husk.dsl.CLI;
import com.sirolf2009.husk.dsl.ParseException;
import com.sirolf2009.husk.dsl.SimpleNode;

public class Husk {

	private List<CommandMethod> allCommands;
	private Map<String, List<CommandMethod>> commands;
	private Object handler;
	private String name;
	private String splash;
	private BuiltinHandler builtinHandler;
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
		allCommands = new ArrayList<CommandMethod>();
		commands = new HashMap<String, List<CommandMethod>>();
		registerMethodHandler(builtinHandler);
		registerMethodHandler(handler);
	}

	public void commandLoop() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		if(splash != null && !splash.isEmpty()) {
			System.out.println(splash);
		}
		while(true) {
			try {
				System.out.print(name+"> ");
				CommandResults result = execute(reader.readLine());
				if(result.getMethod().getMethod() != null && !result.getMethod().getMethod().getReturnType().equals(void.class)) {
					System.out.println(result.getReturnValue());
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (CommandNotFoundException e) {
				System.err.println(e.getMessage());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void registerMethodHandler(Object handler) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		for(Field field : handler.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(HuskReference.class)) {
				if(!field.isAccessible() && field.getAnnotation(HuskReference.class).allowChangeVisibility()) {
					field.setAccessible(true);
					field.set(handler, this);
					field.setAccessible(false);
				} else {
					field.set(handler, this);
				}
			}
		}
		for(Method method : handler.getClass().getMethods()) {
			if(method.isAnnotationPresent(Command.class)) {
				allCommands.add(new CommandMethod(null, null, method.getAnnotation(Command.class).description(), method));
			}
		}
		for(CommandMethod method : allCommands) {
			if(method.getName() == null) {
				String name = method.getAnnotatedFullName();
				if(name != null && !name.isEmpty()) {
					trySaveCommand(name, method);
					method.setName(name);
				}
			}
		}
		for(CommandMethod method : allCommands) {
			if(method.getName() == null) {
				trySaveCommand(method.getMethod().getName(), method);
				method.setName(method.getMethod().getName());
			}
		}
		for(CommandMethod method : allCommands) {
			if(method.getAbbrev() == null) {
				String abbrev = method.getAnnotatedAbbrev();
				if(abbrev != null && !abbrev.isEmpty()) {
					trySaveCommand(abbrev, method);
					method.setAbbrev(abbrev);
				}
			}
		}
		for(CommandMethod method : allCommands) {
			if(method.getAbbrev() == null) {
				int i = 0;
				while(true) {
					i++;
					try {
						String abbrev = getAbbrev(method.getMethod().getName(), i);
						trySaveCommand(abbrev, method);
						method.setAbbrev(abbrev);
						break;
					} catch(CommandSaveException e) {
					} catch(IndexOutOfBoundsException e) {
						break;
					}
				}
			}
		}
	}

	public CommandResults execute(String command) throws CommandNotFoundException, ParseException, IllegalAccessException, InvocationTargetException {
		return execute(CLI.parse(command));
	}

	public CommandResults execute(SimpleNode simplenode) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		if(simplenode.jjtGetNumChildren() > 0) {
			CommandResults result = executeCommand((SimpleNode) simplenode.jjtGetChild(0));
			for(int i = 1; i < simplenode.jjtGetNumChildren(); i++) {
				result = executeCommand((SimpleNode) simplenode.jjtGetChild(i), new Object[] {result.returnValue});
			}
			return result;
		}
		return null;
	}

	public CommandResults executeCommand(SimpleNode simplenode) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		return executeCommand(simplenode, getParameters(simplenode));
	}

	public CommandResults executeCommand(SimpleNode simplenode, Object[] parameters) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		return executeCommand(getWord((SimpleNode) simplenode.jjtGetChild(0)), parameters);
	}

	public CommandResults executeCommand(String name, Object[] parameters) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		try {
			return executeCommand(name, parameters, builtinHandler);
		} catch(CommandNotFoundException e) {}
		return executeCommand(name, parameters, handler);
	}

	public CommandResults executeCommand(String name, Object[] parameters, Object handler) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		if(commands.get(name) != null) {
			for(CommandMethod method : commands.get(name)) {
				try {
					Object object = method.getMethod().invoke(handler, parameters);
					return new CommandResults(object, method);
				} catch (IllegalArgumentException | ArrayStoreException e) {
				}
			};
			for(CommandMethod method : commands.get(name)) {
				try {
					Parameter[] methodParams = method.getMethod().getParameters();
					if(methodParams.length >= 1 && methodParams[methodParams.length-1].getType().isArray()) {
						Object[] arrayParameter = Arrays.copyOfRange(parameters, methodParams.length-1, parameters.length);
						Object[] newParameters = Arrays.copyOf(parameters, methodParams.length);
						newParameters[methodParams.length-1] = getArray(arrayParameter, methodParams[methodParams.length-1].getType().getComponentType());
						Object object = method.getMethod().invoke(handler, newParameters);
						return new CommandResults(object, method);
					}
				} catch (IllegalArgumentException | ArrayStoreException e) {
				}
			};
		}
		throw new CommandNotFoundException(name, parameters);
	}

	private void trySaveCommand(String name, CommandMethod method) throws CommandSaveException {
		CommandMethod colliding = getCollidingCommand(name, method);
		if(colliding == null) {
			commands.get(name).add(method);
		} else {
			throw new CommandSaveException(name, method, colliding);
		}
	}

	private CommandMethod getCollidingCommand(String name, CommandMethod method) {
		if(!commands.containsKey(name)) {
			commands.put(name, new ArrayList<CommandMethod>());
			return null;
		} else {
			int parameters = method.getMethod().getParameterCount();
			for(CommandMethod methodExisting : commands.get(name)) {
				if(methodExisting.getMethod().getParameterCount() == parameters) {
					Parameter[] myParameters = method.getMethod().getParameters();
					Parameter[] hisParameters = methodExisting.getMethod().getParameters();
					int matching = 0;
					for(int j = 0; j < parameters; j++) {
						if(myParameters[j].getType().equals(hisParameters[j].getType())) {
							matching++;
						}
					}
					if(matching == parameters) {
						return methodExisting;
					}
				}
			}
			return null;
		}
	}

	private String getAbbrev(String fullName, int letters) {
		String[] words = fullName.split("(?=\\p{Upper})");
		StringBuilder builder = new StringBuilder();
		for(String word : words) {
			builder.append(word.subSequence(0, letters).toString().toLowerCase());
		}
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private <T> T[] getArray(Object[] array, Class<T> target) {
		T[] newArray = (T[]) Array.newInstance(target, array.length);
		for(int i = 0; i < array.length; i++) {
			newArray[i] = (T) array[i];
		}
		return newArray;
	}

	private Object[] getParameters(SimpleNode simplenode) {
		Object[] parameters = new Object[simplenode.jjtGetNumChildren()-1];
		for(int i = 0; i < simplenode.jjtGetNumChildren()-1; i++) {
			parameters[i] = ((SimpleNode)simplenode.jjtGetChild(i+1)).jjtGetValue();
		}
		return parameters;
	}

	private String getWord(SimpleNode simplenode) {
		return (String) simplenode.jjtGetValue();
	}

	public List<CommandMethod> getAllCommands() {
		return allCommands;
	}

	public void setAllCommands(List<CommandMethod> allCommands) {
		this.allCommands = allCommands;
	}

	public Map<String, List<CommandMethod>> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, List<CommandMethod>> commands) {
		this.commands = commands;
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

	public static class CommandResults {

		private Object returnValue;
		private CommandMethod method;

		public CommandResults(Object returnValue, CommandMethod method) {
			this.returnValue = returnValue;
			this.method = method;
		}

		public Object getReturnValue() {
			return returnValue;
		}
		public void setReturnValue(Object returnValue) {
			this.returnValue = returnValue;
		}
		public CommandMethod getMethod() {
			return method;
		}
		public void setMethod(CommandMethod method) {
			this.method = method;
		}

	}

	public static class CommandNotFoundException extends Exception {

		private static final long serialVersionUID = 2779723193855376315L;

		private String name;
		private Object[] params;

		public CommandNotFoundException(String name, Object[] params) {
			super("Could not find the command "+name+" with parameters "+Arrays.toString(params));
			this.name = name;
			this.params = params;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object[] getParams() {
			return params;
		}

		public void setParams(Object[] params) {
			this.params = params;
		}

	}

	public static class CommandSaveException extends Exception {

		private static final long serialVersionUID = -7168945547567388120L;

		private String name;
		private CommandMethod method;
		private CommandMethod collider;

		public CommandSaveException(String name, CommandMethod method, CommandMethod collider) {
			super(name+" for method "+method+" is already in use by "+collider);
			this.name = name;
			this.method = method;
			this.collider = collider;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public CommandMethod getMethod() {
			return method;
		}

		public void setMethod(CommandMethod method) {
			this.method = method;
		}

		public CommandMethod getCollider() {
			return collider;
		}

		public void setCollider(CommandMethod collider) {
			this.collider = collider;
		}

	}

}