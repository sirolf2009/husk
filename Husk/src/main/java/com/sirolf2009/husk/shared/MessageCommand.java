package com.sirolf2009.husk.shared;

import com.sirolf2009.husk.shared.server.Client;
import com.sirolf2009.husk.shared.server.HuskServer;

public class MessageCommand extends Message {

	private static final long serialVersionUID = -7896776881663280104L;
	
	private String command;
	
	public MessageCommand(String command) {
		this.command = command;
	}
	
	@Override
	public void onServer(HuskServer server, Client client) {
		server.execute(client, this);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
