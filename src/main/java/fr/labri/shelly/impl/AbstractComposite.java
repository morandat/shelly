package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Item;
import fr.labri.shelly.Visitor;

public abstract class AbstractComposite<C, M> extends AbstractItem<C, M> implements Composite<C, M> {

	protected final C _clazz;
	protected final List<Item<C, M>> commands = new ArrayList<Item<C, M>>();

	public AbstractComposite(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
		super(name, parent, annotations);
		_clazz = clazz;
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
		for (Item<C, M> cmd : commands)
			cmd.accept(visitor);
	}

	public void addItem(Item<C, M> cmd) {
		if (cmd != null)
			commands.add(cmd);
	}

	@Override
	public C getAssociatedElement() {
		return _clazz;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public Iterable<Item<C, M>> getItems() {
		return commands;
	}
	
	@Override
	public Object instantiateObject(Object parent) {
		return null;
	}
}