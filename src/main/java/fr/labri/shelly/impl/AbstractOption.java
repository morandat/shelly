package fr.labri.shelly.impl;

import fr.labri.shelly.Context;
import fr.labri.shelly.Description;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption<C, M> implements Option<C, M> {
	final String _id;
	final Context<C, M> _parent;

	public interface OptionAdapter<C, M> {
		abstract Object apply(Option<C, M> opt, Object receive, Object value);
		abstract Description getDescription();
	}

	AbstractOption(Context<C, M> parent, String name) {
		_id = name;
		_parent = parent;
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
	}

	@Override
	public boolean isValid(String str) {
		return ("--" + _id).equals(str);
	}

	public String getID() {
		return _id;
	}

	@Override
	public Context<C, M> getParent() {
		return _parent;
	}
}
