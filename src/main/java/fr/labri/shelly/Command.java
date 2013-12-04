package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;


public interface Command {
	public abstract Command isValid(String str);
	public abstract void apply(Object grp, String cmd, PeekIterator cmdLine);
	public abstract void parse(OptionGroup grp, String cmdText, PeekIterator cmdLine);
	public abstract String toHelpString();
}
