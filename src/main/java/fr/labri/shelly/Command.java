package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;


public interface Command extends ShellyItem {
	public abstract void apply(Object grp, String cmd, PeekIterator<String> cmdLine);
	public abstract void parse(Object parent, String cmdText, PeekIterator<String> cmdLine);
	
	public abstract String[] getHelpString();
}
