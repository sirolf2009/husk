package com.sirolf2009.husk.shared.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;

import com.sirolf2009.husk.Husk;
import com.sirolf2009.husk.shared.Message;
import com.sirolf2009.husk.shared.MessageCommand;
import com.sirolf2009.husk.shared.MessageFinished;

public class HuskClient {
	
	private Husk husk;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public HuskClient(Husk husk, Socket socket) throws IOException {
		this.husk = husk;
		this.socket = socket;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}
	
	public Optional<Object> executeCommand(String command) throws IOException {
		out.writeObject(new MessageCommand(command));
		while(true) {
			try {
				Message message = (Message) in.readObject();
				message.onClient(this);
				if(message instanceof MessageFinished) {
					return Optional.ofNullable(((MessageFinished)message).getResult());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

	public Husk getHusk() {
		return husk;
	}

	public void setHusk(Husk husk) {
		this.husk = husk;
	}

}
