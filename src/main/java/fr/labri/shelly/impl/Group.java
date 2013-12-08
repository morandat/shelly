package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.impl.Visitor.InstVisitor;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;

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

	@Override
	public Command getDefault() {
		try {
			Visitor v = new CommandVisitor() {
				@Override
				public void visit(Command grp) {
					if (grp.isDefault()) {
						throw new FoundCommand(grp);
					}
				}
			};
			visit_commands(v);
		} catch (FoundCommand e) {
			return e.cmd;
		}
		return null;
	}

	@Override
	public boolean isDefault() {
		return _clazz.isAnnotationPresent(Default.class);
	}
}
