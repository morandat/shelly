package fr.labri.shelly;

import fr.labri.shelly.impl.Executor;


public interface Triggerable<C, M> extends Item<C, M> {
	public int isValid(String str, int index);

	Object apply(Object receive, String string, Executor executor);
	Description getDescription();
}
