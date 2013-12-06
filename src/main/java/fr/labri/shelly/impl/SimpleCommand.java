package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Command;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.OptionGroup;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Visitor;

class SimpleCommand implements Command {
	final OptionGroup _parent;
	final String _id;
	final Method _method;
	final String _description = "No description";
	final Converter<?>[] _converters;

	SimpleCommand(ConverterFactory factory, OptionGroup parent, String name, Method method) {
		_id = name;
		_method = method;
		_parent = parent;
		int i = 0;
		Class<?>[] params = method.getParameterTypes();
		_converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			_converters[i++] = factory.getConverter(a, method);
	}

	public Command isValid(String str) {
		return _id.equals(str) ? this : null;
	}

	public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
		try {
			_method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(_converters, cmd, cmdLine));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void parse(Object parent, String cmdText, PeekIterator<String> cmdLine) {
		Object optGrp = _parent.fillOptions(parent, cmdLine);
		apply(optGrp, cmdText, cmdLine);
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
	public ShellyItem getParent() {
		return _parent;
	}
}