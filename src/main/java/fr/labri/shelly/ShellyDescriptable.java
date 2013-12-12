package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface ShellyDescriptable<C, M> extends ShellyItem<C, M> {
	Description getDescription();
	Object apply(Object receive, String string, PeekIterator<String> _cmdline);
}
