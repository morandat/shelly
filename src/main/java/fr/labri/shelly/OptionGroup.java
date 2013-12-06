package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface OptionGroup extends ShellyItem {
	public abstract Class<?> getAssociatedClass();
	public void execute(Object parent, PeekIterator<String> cmdLine);
	public abstract Object fillOptions(Object parent, PeekIterator<String> cmdLine);
	public abstract OptionGroup getParent();
	
	public void visit_options(Visitor visitor);
	public void visit_commands(Visitor visitor);
}