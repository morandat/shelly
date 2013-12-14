package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption<C, M>  extends AbstractTerminal<C, M> implements Option<C, M> {

	protected AbstractOption(Composite<C, M> parent, String name, M item, Annotation[] annotations) {
		super(name, parent, item, annotations);
	}
	
	@Override
	public void startVisit(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return "option " + getID();
	}
}
