package fr.labri.shelly;

import fr.labri.shelly.impl.StringUtils;

public interface Recognizer {

	boolean strictOptions();
	boolean stopOptionParsing(String cmd);
	
	int isLongOption(String cmd);
	int isShortOption(String cmd);

	int isLongOptionValid(String cmd, Option<?, ?> option);
	int isShortOptionValid(String cmd, Option<?, ?> option);
	int isActionValid(String cmd, Action<?, ?> option);
	
	// Assume isLongOption == true
	boolean getBooleanValue(String cmd);
	int isLongBooleanOptionValid(String str, Option<?, ?> option);
	
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
	
	public static Recognizer DEFAULT = GNUNonStrict;

	public static abstract class AbstractRecognizer implements Recognizer {
		protected boolean stictOptions = false;
		protected String optionStopper = "--";
		protected String longOptionPrefix = "--";
		protected String shortOptionPrefix = "";
		protected String no_flag = "no-";

		@Override
		public boolean stopOptionParsing(String cmd) {
			return optionStopper.equals(cmd);
		}
		@Override
		public int isLongOptionValid(String cmd, Option<?, ?> option) {
//			return option.isValid(this, cmd, longOptionPrefix.length()) == cmd.length();
			return StringUtils.startWith(cmd, option.getID(), longOptionPrefix.length());
		}
		
		@Override
		public int isShortOptionValid(String cmd, Option<?, ?> option) {
			return -1; // TODO adapter.isValidShort()
		}

		@Override
		public int isActionValid(String cmd, Action<?, ?> action) {
			return StringUtils.startWith(cmd, action.getID(), 0);
		}
		
		
		@Override
		public boolean strictOptions() {
			return stictOptions;
		}
		
		@Override
		public int isLongOption(String cmd) {
			return (cmd.length() > longOptionPrefix.length()) ? cmd.startsWith(longOptionPrefix) ? longOptionPrefix.length() : -1 : -1;
		}
		
		@Override
		public int isShortOption(String cmd) {
			return cmd.length() > 1 ? (cmd.charAt(0) == '-' && cmd.charAt(1) != '-') ? 1  : -1 : -1;
		}	

		@Override
		public boolean getBooleanValue(String cmd) {
			return !cmd.startsWith(no_flag, longOptionPrefix.length());
		}
		
		@Override
		public int isLongBooleanOptionValid(String str, Option<?, ?> option) {
			int start = longOptionPrefix.length();
			if(str.startsWith(no_flag, start))
				start += no_flag.length();
			
			return start = StringUtils.startWith(str, option.getID(), start);
		}
	}
}
