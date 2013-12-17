package fr.labri.shelly;

import fr.labri.shelly.impl.ParserFactory.AbstractRecognizer;

public interface Recognizer {

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
	
	public static final Recognizer GNUNonStrict = new AbstractRecognizer() {

	};
	public static final Recognizer GNUStrict = new AbstractRecognizer() {
		// TODO maybe rename BSD
		{
			stictOptions = true;
		}
	};
	public static Recognizer Java = new AbstractRecognizer() {
		{
			longOptionPrefix = "-";
			shortOptionPrefix = null;
		}
	};
}
