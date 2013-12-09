package fr.labri.shelly.impl;

import fr.labri.shelly.Context;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption implements fr.labri.shelly.Option {
	final String _id;
	final Context _parent;

	AbstractOption(Context parent, String name) {
		_id = name;
		_parent = parent;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void visit_all(Visitor visitor) {
	}

	@Override
	public boolean isValid(String str) {
		return ("--" + _id).equals(str);
	}

	public String getID() {
		return _id;
	}

	@Override
	public Context getParent() {
		return _parent;
	}
}
