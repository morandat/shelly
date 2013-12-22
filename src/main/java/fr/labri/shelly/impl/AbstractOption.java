package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Option;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption<C, M>  extends AbstractTerminal<C, M> implements Option<C, M> {
	final String flags;
	
	protected AbstractOption(Composite<C, M> parent, String name, M item, Annotation[] annotations) {
		super(name, parent, item, annotations);
		fr.labri.shelly.annotations.Option option = AnnotationUtils.getAnnotation(annotations, fr.labri.shelly.annotations.Option.class);
		if (option == null)
			flags = null;
		else
			flags = option.flags();
	}
	
	@Override
	public boolean isValidShortOption(Recognizer parser, char str) {
		return parser.isShortOptionValid(str, this);
	}

	
	@Override
	public String getFlags() {
		return flags;
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
