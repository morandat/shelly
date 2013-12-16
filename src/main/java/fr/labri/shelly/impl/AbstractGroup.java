package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Triggerable;
import fr.labri.shelly.Visitor;

public abstract class AbstractGroup<C, M> extends AbstractComposite<C, M> implements Group<C, M>, Triggerable<C,M> {
	public AbstractGroup(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
		super(parent, name, clazz, annotations);
	}

	@Override
	public void accept(Visitor<C, M> visitor) {
		visitor.visit((Group<C, M>) this);
	}

	@Override
	public void startVisit(Visitor<C, M> visitor) {
		visitor.startVisit(this);
	}
	
	@Override
	public int isValid(Parser parser, String str, int index) {
		return StringUtils.startWith(str, _id, index);
	}
	
	@Override
	public void executeAction(Object receive, String string, Executor executor) {
	}

	@Override
	public String toString() {
		return "group " + getID();
	}
}