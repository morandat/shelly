package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface ShellyDescriptable extends ShellyItem {
	Description getDescription();
	Object apply(Object receive, String next, PeekIterator<String> _cmdline);
}
