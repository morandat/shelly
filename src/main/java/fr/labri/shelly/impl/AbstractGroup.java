package fr.labri.shelly.impl;

import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;

public abstract class AbstractGroup<C, M> extends AbstractContext<C, M> implements Group<C, M>, ShellyDescriptable<C,M> {
	public AbstractGroup(Context<C, M> parent, String name, C clazz) {
		super(parent, name, clazz);
	}

	@Override
	public void accept(Visitor<C, M> visitor) {
		visitor.visit((Group<C, M>) this);
	}
}