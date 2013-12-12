package fr.labri.shelly;

public interface Item<C, M> {
	public abstract String getID();
	public abstract Composite<C, M> getParent();

	public abstract boolean isValid(String str);

	public abstract void accept(Visitor<C, M> visitor);
	public abstract void visit_all(Visitor<C, M> visitor);
}
