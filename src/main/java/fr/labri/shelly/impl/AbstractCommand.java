package fr.labri.shelly.impl;


import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Visitor;

public abstract class AbstractCommand<C, M> implements Command<C, M> {

	protected final Context<C, M> _parent;
	protected final String _id;
	
	public AbstractCommand(String name, Context<C, M> parent) {
		_id = name;
		_parent = parent;
	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}

	public String getID() {
		return _id;
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
	}

	@Override
	public Context<C, M> getParent() {
		return _parent;
	}
}