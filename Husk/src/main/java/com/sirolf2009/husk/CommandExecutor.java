package com.sirolf2009.husk;

import java.util.Optional;

public interface CommandExecutor {

	public void execute(String command) throws Exception;
	public Optional<Object> executeForResult(String command) throws Exception;

}
