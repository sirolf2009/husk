package com.sirolf2009.husk.shared.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.sirolf2009.husk.shared.Message;

public class Client {

	private Socket socket;
	private String ip;
	private Queue<Message> messageQueue;
	private Thread senderThread;
	private HuskServer server;

	public Client(HuskServer server, Socket socket) {
		this.setSocket(socket);
		this.ip = socket.getInetAddress().getHostAddress();
		this.setServer(server);
		messageQueue = new LinkedList<Message>();
		senderThread = new Thread(() -> {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				while(true) {
					try {
						Thread.sleep(10);
						synchronized(Client.this) {
							try {
								for(Message object : messageQueue) {
									out.writeObject(object);
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
							messageQueue.clear();
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			};
		}, "Sender-"+ip);
		senderThread.setDaemon(true);
		senderThread.start();
		Thread receiverThread = new Thread(() -> {
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				while(true) {
					try {
						Message message = (Message) in.readObject();
						message.onServer(server, this);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "Receiver-"+ip);
		receiverThread.setDaemon(true);
		receiverThread.start();
	}

	public void send(Message... messages) {
		synchronized(Client.this) {
			for(Message message : messages) {
				messageQueue.add(message);
			}
		}
	}

	@Override
	public String toString() {
		return ip;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public HuskServer getServer() {
		return server;
	}

	public void setServer(HuskServer server) {
		this.server = server;
	}

}
