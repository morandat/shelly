package fr.labri.shelly.impl;



import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Visitor;

public abstract class AbstractCommand<C, M> extends AbstractTerminal<C, M> implements Command<C, M> {
	public AbstractCommand(String name, Composite<C, M> parent, M item) {
		super(name, parent, item);
	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}

	@Override
	public void startVisit(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}
	
	@Override
	public Object createContext(Object parent) {
		return null;
	}
}