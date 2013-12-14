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
}
