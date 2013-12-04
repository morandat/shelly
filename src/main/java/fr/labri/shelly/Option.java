package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;


public interface Option {
	abstract public boolean isValid(String str);
	abstract void apply(Object grp, String cmd, PeekIterator cmdLine);
	
	public abstract String toHelpString();
}
