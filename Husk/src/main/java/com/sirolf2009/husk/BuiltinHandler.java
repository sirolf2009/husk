package com.sirolf2009.husk;

import java.util.List;
import java.util.stream.Collectors;

import com.jakewharton.fliptables.FlipTable;

public class BuiltinHandler {
	
	@HuskReference
	private Husk husk;
	
	@Command(fullName="?list", abbrev="?l", description="Display all the commands")
	public String getList() {
		String[] headers = new String[] {"Name", "Abbreviation", "Parameters", "Description"};
		List<String[]> data = husk.getCommandRegister().getAllCommands().parallelStream().map(command -> new String[] {command.getName(), command.getAbbrev(), command.getParameters(), command.getHelpDescription()}).collect(Collectors.toList());
		return FlipTable.of(headers, data.toArray(new String[data.size()][]));
	}

}
