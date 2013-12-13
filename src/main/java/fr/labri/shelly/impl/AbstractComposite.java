package fr.labri.shelly.impl;

import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Action;
import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Option;
import fr.labri.shelly.Item;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;

public abstract class AbstractComposite<C, M> implements Composite<C, M> {

	protected final String _id;
	protected final fr.labri.shelly.Composite<C, M> _parent;

	protected final C _clazz;

	protected final List<Option<C, M>> options = new ArrayList<Option<C, M>>();
	protected final List<Item<C, M>> commands = new ArrayList<Item<C, M>>();

	public AbstractComposite(Composite<C, M> parent, String name, C clazz) {
		_parent = parent;
		_id = name;
		_clazz = clazz;
	}

	public void visit_options(Visitor<C, M> visitor) {
		for (Option<C, M> cmd : options)
			cmd.accept(visitor);
	}

	public void visit_commands(Visitor<C, M> visitor) {
		for (Item<C, M> cmd : commands)
			cmd.accept(visitor);
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
		visit_options(visitor);
		visit_commands(visitor);
	}

	public void addOption(Option<C, M> option) {
		options.add(option);
	}

	public void addCommand(Item<C, M> cmd) {
		commands.add(cmd);
	}

	@Override
	public boolean isValid(String str) {
		return getID().equals(str);
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
	public Iterable<Option<C, M>> getOptions() {
		return options;
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
			visit_commands(v);
		} catch (FoundCommand e) {
			return (Action<C, M>) e.cmd;
		}
		return null;
	}
	
	@Override
	public Object newGroup(Object parent) {
		return null;
	}

	@Override
	public Object getEnclosing(Object obj) {
		return null;
	}
}