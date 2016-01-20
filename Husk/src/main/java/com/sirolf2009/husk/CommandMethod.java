package com.sirolf2009.husk;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CommandMethod implements Serializable {

	private static final long serialVersionUID = 728888558669820405L;
	private String name;
	private String abbrev;
	private String helpDescription;
	private transient Method method;

	public CommandMethod(String name, String abbrev, String helpDescription, Method method) {
		this.name = name;
		this.abbrev = abbrev;
		this.helpDescription = helpDescription;
		this.method = method;
	}

	public String getParameters() {
		if(method.getParameterCount() > 0) {
			return Arrays.asList(method.getParameterTypes()).parallelStream().map(type -> getParameter(type)).reduce((type1, type2) -> type1+", "+type2).get();
		} else {
			return "";
		}
	}

	private String getParameter(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	public String getAnnotatedDescription() {
		return getCommand().description();
	}

	public String getAnnotatedFullName() {
		return getCommand().fullName();
	}

	public String getAnnotatedAbbrev() {
		return getCommand().abbrev();
	}

	public Command getCommand() {
		return method.getAnnotation(Command.class);
	}

	@Override
	public String toString() {
		return getMethod().getName()+"("+getParameters()+")";
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAbbrev() {
		return abbrev;
	}
	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}
	public String getHelpDescription() {
		return helpDescription;
	}
	public void setHelpDescription(String helpDescription) {
		this.helpDescription = helpDescription;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}

}
