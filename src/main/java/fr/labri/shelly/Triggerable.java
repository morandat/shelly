package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface Triggerable<C, M> extends Item<C, M> {
	Object apply(Object receive, String string, PeekIterator<String> _cmdline);
	Description getDescription();
}
