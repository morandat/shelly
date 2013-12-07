package fr.labri.shelly.impl;


import fr.labri.shelly.Context;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.InstVisitor;

public class Group extends fr.labri.shelly.impl.Context implements fr.labri.shelly.Group, ShellyDescriptable {
	public Group(Context parent, String name, Class<?> clazz) {
		super(parent, name, clazz);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit((Group) this);
	}

	@Override
	public String[] getHelpString() {
		return new String[] { _id, "no description" };
	}

	@Override
	public void apply(Object receive, String next, PeekIterator<String> _cmdline) {
	}

	@Override
	public Object createContext(Object parent) {
		return new InstVisitor().instantiate(this, parent);
	}
}
