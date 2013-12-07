package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface Context extends ShellyItem {
	public abstract Class<?> getAssociatedClass();
	public void execute(Object parent, PeekIterator<String> cmdLine);
	public abstract Object fillOptions(Object parent, PeekIterator<String> cmdLine);
	public abstract Context getParent();
	
	public void visit_options(Visitor visitor);
	public void visit_commands(Visitor visitor);
}