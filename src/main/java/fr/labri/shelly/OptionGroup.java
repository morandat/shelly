package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface OptionGroup {

	public abstract Object fillOptions(PeekIterator cmdLine);

}