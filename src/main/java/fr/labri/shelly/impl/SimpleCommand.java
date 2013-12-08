package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.annotations.Default;

class SimpleCommand extends AbstractCommand {
	final Method _method;
	SimpleCommand(ConverterFactory factory, Context parent, String name, Method method) {
		super(name, parent, factory, method.getParameterTypes());
		_method = method;
	}

	@Override
	public boolean isDefault() {
		return _method.isAnnotationPresent(Default.class);
	}
	
	public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
		try {
			_method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(_converters, cmd, cmdLine));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}