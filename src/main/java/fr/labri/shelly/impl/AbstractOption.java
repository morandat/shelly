package fr.labri.shelly.impl;

import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Description;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;

public abstract class AbstractOption implements Option {
	final String _id;
	final Context _parent;
	final String _description = "No description";
	final Converter<?> _converter;

	AbstractOption(Converter<?> converter, Context parent, String name) {
		_id = name;
		_parent = parent;
		_converter = converter;
	}
	
	public boolean isValid(String str) {
		return ("--" + _id).equals(str);
	}


	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public String[] getHelpString() {
		return new String[] { "--"+_id, _description };
	}

	@Override
	public void visit_all(Visitor visitor) {
	}
	

	public String getID() {
		return _id;
	}
	
	@Override
	public Context getParent() {
		return _parent;
	}
	
	public static AbstractOption getOption(String name, Context parent, Converter<?> converter, final OptionAdapter adapter) {
		return new AbstractOption(converter, parent, name) {
			@Override
			public void apply(Object receive, String next, PeekIterator<String> _cmdline) {
				adapter.apply(this, receive, next, _cmdline);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	public interface OptionAdapter {
		void apply(AbstractOption opt, Object receive, String next, fr.labri.shelly.impl.PeekIterator<String> _cmdline);
		Description getDescription();
	}
}
