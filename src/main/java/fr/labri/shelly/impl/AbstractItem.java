package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Item;

public abstract class AbstractItem<C, M> implements Item<C, M> {

	protected final Composite<C, M> _parent;
	protected final String _id;

	public AbstractItem(String name, Composite<C, M> parent) {
		_id = name;
		_parent = parent;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public Composite<C, M> getParent() {
		return _parent;
	}
}
