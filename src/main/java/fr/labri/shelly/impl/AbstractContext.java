package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Visitor;

public class AbstractContext<C, M> extends  AbstractComposite<C, M> implements fr.labri.shelly.Context<C, M>{
	public AbstractContext(Composite<C, M> parent, String name, C clazz) {
		super(parent, name, clazz);
	}

	@Override
	public void startVisit(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}
}
