package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;


public interface ShellyItem {
	public abstract String getID();
	public abstract ShellyItem getParent();
	
	public abstract ShellyItem isValid(String str);
	public abstract void apply(Object grp, String cmd, PeekIterator<String> cmdLine);
	public abstract void parse(Object parent, String cmdText, PeekIterator<String> cmdLine);

	public abstract void accept(Visitor visitor);
	public abstract void visit_all(Visitor visitor);
}
