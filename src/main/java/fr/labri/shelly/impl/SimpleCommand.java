package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Command;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.OptionGroup;

class SimpleCommand implements Command {
	final String _name;
	final Method _method;
	final String _description = "No description";
	final Converter<?>[] _converters;
	
	SimpleCommand(ConverterFactory factory, String name, Method method) {
		_name = name;
		_method = method;
		int i = 0;
		Class<?>[] params = method.getParameterTypes();
		_converters = new Converter<?>[params.length];
		for(Class<?> a: params)
			_converters[i++] = factory.getConverter(a, method);
	}
	
	public Command isValid(String str) {
		return _name.equals(str) ? this : null;
	}

	public void apply(Object grp, String cmd, PeekIterator cmdLine) {
		try {
			_method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(_converters, cmd, cmdLine));
		} catch (IllegalArgumentException|IllegalAccessException|InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void parse(OptionGroup grp, String cmdText, PeekIterator cmdLine) {
		Object optGrp = grp.fillOptions(cmdLine);
		apply(optGrp, cmdText, cmdLine);
	}
	
	public String toHelpString() {
		return String.format("%s: %s", _name, _description);
	}
}