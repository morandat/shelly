package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.impl.Visitor.InstVisitor;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;

public abstract class AbstractGroup extends AbstractContext implements Group, ShellyDescriptable {
	public AbstractGroup(Context parent, String name, Class<?> clazz) {
		super(parent, name, clazz);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit((Group) this);
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
	public Description getDescription() {
		return DescriptionFactory.getDescription(_clazz, AnnotationUtils.getGroupSummary(_clazz));
	}
}
