package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption<C, M>  extends AbstractTerminal<C, M> implements Option<C, M> {

	protected AbstractOption(Composite<C, M> parent, String name, M item) {
		super(name, parent, item);
	}
	
	@Override
	public void startVisit(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
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
}
