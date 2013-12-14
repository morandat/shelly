package fr.labri.shelly.impl;

import fr.labri.shelly.Action;
import fr.labri.shelly.Option;

public class ParserFactory {

	static abstract class AbstractExecutorModel implements Parser {
		boolean stictOptions = false;
		String optionStopper = "--";
		String longOptionPrefix = "--";
		String shortOptionPrefix = "";
		@Override
		public boolean stopParsing(String cmd) {
			return optionStopper.equals(cmd);
		}
		@Override
		public boolean isValid(String cmd, Option<?, ?> option) {
			return cmd.startsWith(longOptionPrefix) && option.isValid(cmd, longOptionPrefix.length()) > 0;
		}
		
		@Override
		public boolean isValid(String cmd, Action<?, ?> action) {
			return action.isValid(cmd, 0) >= 0;
		}
		
	}
	public static final Parser GNUNonStrict = new AbstractExecutorModel() {
	};
	public static final Parser GNUStrict = new AbstractExecutorModel() {
		{
			stictOptions = true;
		}
		// TODO maybe rename BSD
	};
	public static Parser Java = new AbstractExecutorModel() {
		{
			longOptionPrefix = "-";
			shortOptionPrefix = null;
		}
//			strictOption	
	};
	
}