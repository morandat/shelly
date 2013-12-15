package fr.labri.shelly.impl;

import fr.labri.shelly.Action;
import fr.labri.shelly.Option;

public interface Parser {

	boolean strictOptions();
	boolean stopOptionParsing(String cmd);
	boolean isValid(String cmd, Option<?, ?> option);
	boolean isValid(String cmd, Action<?, ?> option);

}
