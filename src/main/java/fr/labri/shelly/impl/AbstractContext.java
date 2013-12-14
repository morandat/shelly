package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Visitor;

public class AbstractContext<C, M> extends  AbstractComposite<C, M> implements fr.labri.shelly.Context<C, M>{
	public AbstractContext(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
		super(parent, name, clazz, annotations);
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
		return "context " + getID();
	}
}
