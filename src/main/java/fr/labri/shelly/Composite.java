package fr.labri.shelly;


public interface Composite<C, M> extends Item<C, M> {
	
	public abstract C getAssociatedElement();

	public abstract void addItem(Item<C, M> cmd);
	public abstract Iterable<Item<C, M>> getItems();

	public abstract Object instantiateObject(Object parent);
	public abstract Object getEnclosingObject(Object obj);
}