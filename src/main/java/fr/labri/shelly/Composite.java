package fr.labri.shelly;

import fr.labri.shelly.impl.Environ;

public interface Composite<C, M> extends Item<C, M> {
	
	public abstract C getAssociatedElement();

	public abstract void addItem(Item<C, M> cmd);
	public abstract Iterable<Item<C, M>> getItems();
	public abstract void visit_all(Visitor<C, M> visitor);

	public abstract boolean isEnclosed();
	public abstract void instantiateObject(Environ environ);
}