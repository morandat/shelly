package fr.labri.shelly.impl;

import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Action;
import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Item;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;

public abstract class AbstractComposite<C, M> implements Composite<C, M> {

	protected final String _id;
	protected final fr.labri.shelly.Composite<C, M> _parent;

	protected final C _clazz;

	protected final List<Item<C, M>> commands = new ArrayList<Item<C, M>>();

	public AbstractComposite(Composite<C, M> parent, String name, C clazz) {
		_parent = parent;
		_id = name;
		_clazz = clazz;
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
		for (Item<C, M> cmd : commands)
			cmd.accept(visitor);
	}

	public void addItem(Item<C, M> cmd) { //FIXME rename to add, and commands to items
		commands.add(cmd);
	}

	@Override
	public C getAssociatedElement() {
		return _clazz;
	}

	@Override
	public Composite<C, M> getParent() {
		return _parent;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public Iterable<Item<C, M>> getItems() {
		return commands;
	}

	@SuppressWarnings("unchecked")
	public Action<C, M> getDefault() {
		try {
			Visitor<C, M> v = new CommandVisitor<C, M>() {
				@Override
				public void visit(Command<C, M> grp) {
					if (grp.isDefault()) {
						throw new FoundCommand(grp);
					}
				}
			};
			visit_all(v);
		} catch (FoundCommand e) {
			return (Action<C, M>) e.cmd;
		}
		return null;
	}
	
	@Override
	public Object instantiateObject(Object parent) {
		return null;
	}

	@Override
	public Object getEnclosingObject(Object obj) {
		return null;
	}
}