package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.InstVisitor;

public abstract class AbstractCommand implements Command {

	protected final Context _parent;
	protected final String _id;
	
	public AbstractCommand(String name, Context parent) {
		_id = name;
		_parent = parent;
	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}

	public String getID() {
		return _id;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void visit_all(Visitor visitor) {
	}

	@Override
	public Context getParent() {
		return _parent;
	}

	@Override
	public Object createContext(Object parent) {
		return new InstVisitor().instantiate(this, parent);
	}
}