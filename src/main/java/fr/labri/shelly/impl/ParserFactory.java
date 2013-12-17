package fr.labri.shelly.impl;

import fr.labri.shelly.Action;
import fr.labri.shelly.Option;
import fr.labri.shelly.Parser;

public class ParserFactory {

	public static abstract class AbstractParser implements Parser {
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
		public boolean isLongOptionValid(String cmd, Option<?, ?> option) {
			return option.isValid(this, cmd, longOptionPrefix.length()) == cmd.length();
		}
		
		@Override
		public boolean isShortOptionValid(String cmd, Option<?, ?> option) {
			return false; // TODO adapter.isValidShort()
		}

		@Override
		public boolean isActionValid(String cmd, Action<?, ?> action) {
			return action.isValid(this, cmd, 0) >= 0;
		}
		
		
		@Override
		public boolean strictOptions() {
			return stictOptions;
		}
		
		@Override
		public boolean isLongOption(String cmd) {
			return cmd.startsWith(longOptionPrefix) && cmd.length() > longOptionPrefix.length();
		}
		
		@Override
		public boolean isShortOption(String cmd) {
			return cmd.length() > 1 ? (cmd.charAt(0) == '-' && cmd.charAt(1) != '-') : false;
		}	

		@Override
		public boolean getBooleanValue(String cmd) {
			return cmd.startsWith(no_flag, longOptionPrefix.length());
		}
		
		@Override
		public int isLongBooleanOptionValid(String str, Option<?, ?> option, int index) {
			if(str.startsWith(no_flag, index))
				index += no_flag.length();
			
			return index = StringUtils.startWith(str, option.getID(), index);
		}
	}
}
