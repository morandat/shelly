package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface ShellyDescriptable extends ShellyItem {
	public abstract String[] getHelpString();
	
	void apply(Object receive, String next, PeekIterator<String> _cmdline);
}
