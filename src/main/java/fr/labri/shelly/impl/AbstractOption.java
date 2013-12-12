package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption<C, M> implements Option<C, M> {
	final String _id;
	final Composite<C, M> _parent;

	AbstractOption(Composite<C, M> parent, String name) {
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
		boolean v = endsWith(str, _id, startWith(str, "--"));
		return v;
	}

	static public int startWith(String str, String prefix) {
		return startWith(str, prefix, 0);
	}
	
	static public int startWith(String str, String prefix, int offset) {
		return str.startsWith(prefix, offset) ? prefix.length() : 0;
	}
	static public boolean endsWith(String str, String suffix, int offset) {
		return (offset + suffix.length() == str.length()) ? str.endsWith(suffix) : false;
	}

	public String getID() {
		return _id;
	}

	@Override
	public Composite<C, M> getParent() {
		return _parent;
	}
}
