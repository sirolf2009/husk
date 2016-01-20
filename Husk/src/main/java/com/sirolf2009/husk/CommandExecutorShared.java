package com.sirolf2009.husk;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Optional;

import com.sirolf2009.husk.CommandExecutorDefault.CommandNotFoundException;
import com.sirolf2009.husk.CommandExecutorDefault.CommandResults;
import com.sirolf2009.husk.dsl.ParseException;
import com.sirolf2009.husk.shared.client.HuskClient;
import com.sirolf2009.husk.shared.server.HuskServer;

public class CommandExecutorShared implements CommandExecutor {

	private Husk husk;
	private int port;
	private String[] IPs;
	private Optional<HuskClient> connection;
	private CommandExecutorDefault defaultExecutor;
	private boolean initialized;

	public CommandExecutorShared(Husk husk, int port, String... IPs) {
		this.setHusk(husk);
		this.port = port;
		this.IPs = IPs;
	}

	@Override
	public void execute(String command) throws CommandNotFoundException, ParseException, IllegalAccessException, InvocationTargetException, IOException {
		init();
		if(connection.isPresent()) {
			connection.get().executeCommand(command);
		} else {
			defaultExecutor.execute(command);
		}
	}
	
	@Override
	public Optional<Object> executeForResult(String command) throws Exception {
		init();
		if(connection.isPresent()) {
			Optional<Object> result = connection.get().executeCommand(command);
			if(result.isPresent()) {
				if(result.get() instanceof CommandResults) {
					return Optional.of(((CommandResults)result.get()).getReturnValue());
				}
				return Optional.of(result.get());
			}
			return Optional.empty();
		} else {
			return defaultExecutor.executeForResult(command);
		}
	}
	
	public void init() throws IOException {
		if(!initialized) {
			connection = getConnection();
			if(!connection.isPresent()) {
				husk.setServer(new HuskServer(husk, port));
				husk.getServer().start();
				defaultExecutor = new CommandExecutorDefault(husk);
			}
			initialized = true;
		}
	}

	public Optional<HuskClient> getConnection() {
		for(String IP : IPs) {
			try {
				return Optional.of(new HuskClient(husk, new Socket(IP, port)));
			} catch (IOException e) {}
		}
		return Optional.empty();
	}

	public Husk getHusk() {
		return husk;
	}

	public void setHusk(Husk husk) {
		this.husk = husk;
	}

}
