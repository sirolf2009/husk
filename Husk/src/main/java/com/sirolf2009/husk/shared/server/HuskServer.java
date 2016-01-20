package com.sirolf2009.husk.shared.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sirolf2009.husk.Husk;
import com.sirolf2009.husk.shared.MessageCommand;
import com.sirolf2009.husk.shared.MessageFinished;

public class HuskServer {

	private ServerSocket socket;
	private List<Client> clients;
	private Husk husk;

	public HuskServer(Husk husk, int port) throws IOException {
		this.husk = husk;
		socket = new ServerSocket(port);
		clients = new ArrayList<Client>();
	}

	public void start() {
		Thread thread = new Thread(() -> {
			while(true) {
				try {
					clients.add(new Client(this, socket.accept()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "Connector");
		thread.setDaemon(true);
		thread.start();
	}

	public void execute(Client client, MessageCommand message) {
		try {
			Optional<Object> results = husk.executeForResult(message.getCommand());
			if(results.isPresent()) {
				if(results.get() instanceof Serializable) {
					client.send(new MessageFinished((Serializable)results.get()));
				} else {
					client.send(new MessageFinished(husk.convert(results.get()).orElse(results.get()+"")));
				}
			} else {
				client.send(new MessageFinished(null));
			}
		} catch(Exception e) {
			client.send(new MessageFinished(e));
		}
	}

	public ServerSocket getSocket() {
		return socket;
	}

	public void setSocket(ServerSocket socket) {
		this.socket = socket;
	}

	public List<Client> getClients() {
		return clients;
	}

	public void setClients(List<Client> clients) {
		this.clients = clients;
	}

	public Husk getHusk() {
		return husk;
	}

	public void setHusk(Husk husk) {
		this.husk = husk;
	}

}
