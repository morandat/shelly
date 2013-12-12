package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;

public interface Group<C, M> extends Composite<C, M>, Action<C, M> {
	Action<C, M> getDefault();
	
	public interface GroupAdapter<C, M> {
		Object apply(Group<C, M> abstractGroup, Object receive, PeekIterator<String> cmdline);
		boolean isDefault();
		Object newGroup(Object parent);
		Object getEnclosing(Object parent);
		Description getDescription(Group<C, M> group);
	}
}
