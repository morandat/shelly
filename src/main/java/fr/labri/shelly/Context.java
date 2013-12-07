package fr.labri.shelly;


public interface Context extends ShellyItem {
	public abstract Class<?> getAssociatedClass();

	public abstract Object newGroup(Object parent);
	public abstract Object getEnclosing(Object obj);
	
	public void visit_options(Visitor visitor);
	public void visit_commands(Visitor visitor);

	public abstract Iterable<Option> getOptions();

}