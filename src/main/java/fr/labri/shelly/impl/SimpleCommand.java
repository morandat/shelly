package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Command;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.impl.Visitor.InstVisitor;

class SimpleCommand implements Command {
	final Context _parent;
	final String _id;
	final Method _method;
	final String _description = "No description";
	final Converter<?>[] _converters;

	SimpleCommand(ConverterFactory factory, Context parent, String name, Method method) {
		_id = name;
		_method = method;
		_parent = parent;
		int i = 0;
		Class<?>[] params = method.getParameterTypes();
		_converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			_converters[i++] = factory.getConverter(a, method);
	}

	public boolean isValid(String str) {
		return _id.equals(str);
	}
	
	public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
		try {
			_method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(_converters, cmd, cmdLine));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
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