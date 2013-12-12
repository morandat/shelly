package fr.labri.shelly.impl;

import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Context;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Visitor;

public abstract class AbstractContext<C, M> implements Context<C, M> {

	protected final String _id;
	protected final fr.labri.shelly.Context<C, M> _parent;

	protected final C _clazz;
	
	private final List<Option<C, M>> options = new ArrayList<Option<C, M>>();
	private final List<ShellyItem<C, M>> commands = new ArrayList<ShellyItem<C, M>>();

	public AbstractContext(Context<C, M> parent, String name, C clazz) {
		_parent = parent;
		_id = name;
		_clazz = clazz;
	}


	public void addOption(Option<C, M> option) {
		options.add(option);
	}

	public void addCommand(ShellyItem<C, M> cmd) {
		commands.add(cmd);
	}

	@Override
	public boolean isValid(String str) {
		return getID().equals(str);
	}

	public void accept(Visitor<C, M> visitor) {
		visitor.visit(this);
	}

	public void visit_options(Visitor<C, M> visitor) {
		for (Option<C, M> cmd : options)
			cmd.accept(visitor);
	}

	public void visit_commands(Visitor<C, M> visitor) {
		for (ShellyItem<C, M> cmd : commands)
			cmd.accept(visitor);
	}

	@Override
	public void visit_all(Visitor<C, M> visitor) {
		visit_options(visitor);
		visit_commands(visitor);
	}

	@Override
	public C getAssociatedElement() {
		return _clazz;
	}

	@Override
	public Context<C, M> getParent() {
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
	public Iterable<ShellyItem<C, M>> getItems() {
		return commands;
	}

	public interface ContextAdapter<C, M> {
		public Object newGroup(Object parent);
		public Object getEnclosing(Object obj);
	}
}