package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Description;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.InstVisitor;

public abstract class AbstractCommand implements Command {

	protected final Context _parent;
	protected final String _id;
	
	protected final Converter<?>[] _converters;
	
	public AbstractCommand(String name, Context parent, fr.labri.shelly.ConverterFactory factory, Class<?>[] params) {
		this(name, parent, ConverterFactory.getConverters(factory, params));
	}
	
	public AbstractCommand(String name, Context parent, fr.labri.shelly.ConverterFactory factory, Class<?> param) {
		this(name, parent, ConverterFactory.getConverters(factory, param));
	}
	
	public AbstractCommand(String name, Context parent, Converter<?>[] converters) {
		_id = name;
		_parent = parent;
		_converters = converters;
	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}

	public String getID() {
		return _id;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void visit_all(Visitor visitor) {
	}

	@Override
	public Context getParent() {
		return _parent;
	}

	@Override
	public Object createContext(Object parent) {
		return new InstVisitor().instantiate(this, parent);
	}
	
	
	static AbstractCommand getCommand(String name, Context parent, Converter<?>[] converters, final CommandAdapter adapter) {
		return new AbstractCommand(name, parent, converters) {
			@Override
			public void apply(Object receive, String next, PeekIterator<String> cmdline) {
				adapter.apply(this, receive, next, cmdline);
			}
			
			@Override
			public boolean isDefault() {
				return adapter.isDefault();
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	public interface CommandAdapter {
		public void apply(AbstractCommand cmd, Object receive, String next, PeekIterator<String> cmdline);
		public Description getDescription();
		public boolean isDefault();
	}
}