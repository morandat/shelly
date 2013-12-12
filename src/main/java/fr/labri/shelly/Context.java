package fr.labri.shelly;


public interface Context<C, M> extends ShellyItem<C, M> {
	
	public abstract C getAssociatedElement();

	public void visit_options(Visitor<C, M> visitor);
	public void visit_commands(Visitor<C, M> visitor);

	public abstract Iterable<Option<C, M>> getOptions();
	public abstract Iterable<ShellyItem<C, M>> getItems();

	void addOption(Option<C, M> opt);
	void addCommand(ShellyItem<C, M> cmd);

	public abstract Object newGroup(Object parent);
	public abstract Object getEnclosing(Object obj);

	public interface ContextAdapter<C, M> {
		public abstract Object newGroup(Object parent);
		public abstract Object getEnclosing(Object obj);
	}
}