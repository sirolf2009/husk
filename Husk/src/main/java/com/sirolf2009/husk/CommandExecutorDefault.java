package com.sirolf2009.husk;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import com.sirolf2009.husk.dsl.CLI;
import com.sirolf2009.husk.dsl.ParseException;
import com.sirolf2009.husk.dsl.SimpleNode;

public class CommandExecutorDefault implements CommandExecutor {
	
	private Husk husk;
	
	public CommandExecutorDefault(Husk husk) {
		this.husk = husk;
	}

	@Override
	public void execute(String command) throws CommandNotFoundException, ParseException, IllegalAccessException, InvocationTargetException {
		Optional<CommandResults> results = executeForCommandResults(command);
		if(results.isPresent() && results.get().getMethod().getMethod() != null && !results.get().getMethod().getMethod().getReturnType().equals(void.class)) {
			System.out.println(husk.convert(results.get().getReturnValue()).orElse(results.get().getReturnValue()+""));
		}
	}
	
	@Override
	public Optional<Object> executeForResult(String command) throws Exception {
		Optional<CommandResults> results = executeForCommandResults(command);
		if(results.isPresent() && results.get().getMethod().getMethod() != null && !results.get().getMethod().getMethod().getReturnType().equals(void.class)) {
			return Optional.ofNullable(results.get().returnValue);
		}
		return Optional.empty();
	}

	public Optional<CommandResults> executeForCommandResults(String command) throws CommandNotFoundException, ParseException, IllegalAccessException, InvocationTargetException {
		return execute(CLI.parse(command));
	}

	public Optional<CommandResults> execute(SimpleNode simplenode) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		if(simplenode.jjtGetNumChildren() > 0) {
			CommandResults result = executeCommand((SimpleNode) simplenode.jjtGetChild(0));
			for(int i = 1; i < simplenode.jjtGetNumChildren(); i++) {
				result = executeCommand((SimpleNode) simplenode.jjtGetChild(i), new Object[] {result.getReturnValue()});
			}
			return Optional.of(result);
		}
		return Optional.empty();
	}

	public CommandResults executeCommand(SimpleNode simplenode) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		return executeCommand(simplenode, getParameters(simplenode));
	}

	public CommandResults executeCommand(SimpleNode simplenode, Object[] parameters) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		return executeCommand(getWord((SimpleNode) simplenode.jjtGetChild(0)), parameters);
	}

	public CommandResults executeCommand(String name, Object[] parameters) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		try {
			return executeCommand(name, parameters, husk.getBuiltinHandler());
		} catch(CommandNotFoundException e) {}
		return executeCommand(name, parameters, husk.getHandler());
	}

	public CommandResults executeCommand(String name, Object[] parameters, Object handler) throws CommandNotFoundException, IllegalAccessException, InvocationTargetException {
		CommandRegister commandRegister = husk.getCommandRegister();
		if(commandRegister.get(name) != null) {
			for(CommandMethod method : commandRegister.get(name)) {
				if(method.getMethod().getParameterCount() == parameters.length) {
					try {
						Parameter[] methodParams = method.getMethod().getParameters();
						Object[] convertedParameters = new Object[parameters.length];
						for(int i = 0; i < parameters.length; i++) {
							convertedParameters[i] = convert(parameters[i], methodParams[i].getType());
						}
						Object object = method.getMethod().invoke(handler, convertedParameters);
						return new CommandResults(object, method);
					} catch (IllegalArgumentException | ArrayStoreException e) {
					}
				}
			};
			for(CommandMethod method : commandRegister.get(name)) {
				try {
					Object object = method.getMethod().invoke(handler, parameters);
					return new CommandResults(object, method);
				} catch (IllegalArgumentException | ArrayStoreException e) {
				}
			};
			for(CommandMethod method : commandRegister.get(name)) {
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

	private Object convert(Object object, Class<?> clazz) {
		for(InputConverter converter : husk.getInputConverters()) {
			Object returned = converter.convert(object, clazz);
			if(returned != null) {
				return returned;
			}
		}
		return object;
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

	public static class CommandResults implements Serializable {

		private static final long serialVersionUID = -4294490738853577676L;

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

}
