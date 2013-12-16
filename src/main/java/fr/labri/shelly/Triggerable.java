package fr.labri.shelly;

import fr.labri.shelly.impl.Executor;
import fr.labri.shelly.impl.Parser;


public interface Triggerable<C, M> extends Item<C, M> {
	public int isValid(Parser parser, String str, int index);

	void executeAction(Object receive, String string, Executor executor);
	Description getDescription();
}
