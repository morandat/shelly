package fr.labri.shelly;

import fr.labri.shelly.impl.ParserFactory.AbstractParser;

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
	
	public static final Parser GNUNonStrict = new AbstractParser() {

	};
	public static final Parser GNUStrict = new AbstractParser() {
		// TODO maybe rename BSD
		{
			stictOptions = true;
		}
	};
	public static Parser Java = new AbstractParser() {
		{
			longOptionPrefix = "-";
			shortOptionPrefix = null;
		}
	};
}
