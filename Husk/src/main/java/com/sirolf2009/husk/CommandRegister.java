package com.sirolf2009.husk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegister {
	
	private List<CommandMethod> allCommands;
	private Map<String, List<CommandMethod>> commands;
	
	public CommandRegister() {
		allCommands = new ArrayList<CommandMethod>();
		commands = new HashMap<String, List<CommandMethod>>();
	}
	
	public void registerMethodHandler(Husk husk, Object handler) throws CommandSaveException, IllegalArgumentException, IllegalAccessException {
		for(Field field : handler.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(HuskReference.class)) {
				if(!field.isAccessible() && field.getAnnotation(HuskReference.class).allowChangeVisibility()) {
					field.setAccessible(true);
					field.set(handler, husk);
					field.setAccessible(false);
				} else {
					field.set(handler, husk);
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
	
	public List<CommandMethod> get(String command) {
		return commands.get(command);
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
