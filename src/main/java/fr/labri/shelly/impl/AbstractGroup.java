package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Triggerable;
import fr.labri.shelly.Visitor;

public abstract class AbstractGroup<C, M> extends AbstractComposite<C, M> implements Group<C, M>, Triggerable<C,M> {
	public AbstractGroup(Composite<C, M> parent, String name, C clazz) {
		super(parent, name, clazz);
	}

	@Override
	public void accept(Visitor<C, M> visitor) {
		visitor.visit((Group<C, M>) this);
	}
}