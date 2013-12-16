package fr.labri.shelly.impl;

import fr.labri.shelly.Action;
import fr.labri.shelly.Option;

public interface Parser {

	boolean strictOptions();
	boolean stopOptionParsing(String cmd);
	
	boolean isLongOption(String cmd);
	boolean isShortOption(String cmd);

	boolean isLongOptionValid(String cmd, Option<?, ?> option);
	boolean isShortOptionValid(String cmd, Option<?, ?> option);
	boolean isActionValid(String cmd, Action<?, ?> option);
	
	// Assume isLongOption == true
	boolean getBooleanValue(String cmd);
	int isLongBooleanOptionValid(String str, Option<?, ?> option, int index);
}
