package com.sirolf2009.husk.shared;

import java.io.Serializable;

import com.sirolf2009.husk.shared.client.HuskClient;
import com.sirolf2009.husk.shared.server.Client;
import com.sirolf2009.husk.shared.server.HuskServer;

public abstract class Message implements Serializable {

	private static final long serialVersionUID = 6658371523223774480L;
	
	public void onServer(HuskServer server, Client client) {}
	public void onClient(HuskClient client) {}

}
