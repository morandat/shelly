package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Context;
import fr.labri.shelly.Visitor;

abstract class SimpleOption implements Option {
	final String _id;
	final Context _parent;
	final String _description = "No description";
	final Converter<?> _converter;

	SimpleOption(Converter<?> converter, Context parent, String name) {
		_id = name;
		_parent = parent;
		_converter = converter;
	}

	static SimpleOption build(ConverterFactory factory, Context parent, String name, final Field field) {
		return new SimpleOption(factory.getConverter(field.getType(), name), parent, name) {
			public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
				try {
					field.set(grp, _converter.convert(cmd, cmdLine));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	static SimpleOption build(ConverterFactory factory, Context parent, String name, final Method method) {
		return new SimpleOption(factory.getConverter(method.getParameterTypes()[0], name), parent, name) { // FIXME, ensure it works
			public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
				try {
					method.invoke(grp, _converter.convert(cmd, cmdLine));
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	public boolean isValid(String str) {
		return ("--" + _id).equals(str);
	}


	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public String[] getHelpString() {
		return new String[] { _id, _description };
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
}