package com.sirolf2009.husk.shared;

import java.io.Serializable;

public class MessageFinished extends Message {

	private static final long serialVersionUID = 137465248932866837L;
	
	private Serializable result;
	
	public MessageFinished(Serializable result) {
		this.result = result;
	}
	
	public Serializable getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

}
