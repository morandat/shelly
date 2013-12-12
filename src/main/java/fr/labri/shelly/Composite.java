package fr.labri.shelly;


public interface Composite<C, M> extends Item<C, M> {
	
	public abstract C getAssociatedElement();

	public void visit_options(Visitor<C, M> visitor);
	public void visit_commands(Visitor<C, M> visitor);

	public abstract Iterable<Option<C, M>> getOptions();
	public abstract Iterable<Item<C, M>> getItems();

	void addOption(Option<C, M> opt);
	void addCommand(Item<C, M> cmd);

	public abstract Object newGroup(Object parent);
	public abstract Object getEnclosing(Object obj);
}