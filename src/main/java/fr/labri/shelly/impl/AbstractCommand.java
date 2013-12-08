package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.InstVisitor;

public abstract class AbstractCommand implements Command{

	protected final Context _parent;
	protected final String _id;
	final String _description = "No description";
	protected final Converter<?>[] _converters;

	public AbstractCommand(String name, Context parent, ConverterFactory factory, Class<?>[] params) {
		_id = name;
		_parent = parent;
		
		int i = 0;
		_converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			_converters[i++] = factory.getConverter(a, name);

	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}

	public String[] getHelpString() {
		return new String[]{_id, _description};
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

}