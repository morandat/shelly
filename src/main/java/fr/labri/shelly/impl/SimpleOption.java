package fr.labri.shelly.impl;

import java.lang.reflect.Field;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Option;

class SimpleOption implements Option {
	final String _name;
	final String _description = "No description";
	final Field _field;
	final Converter<?> _converter;
	
	SimpleOption(ConverterFactory factory, String name, Field field) {
		_name = name;
		_field = field;
		_converter = factory.getConverter(field.getType(), field);
	}
	
	public boolean isValid(String str) {
		return str.equals("--" + _name);
	}

	public void apply(Object grp, String cmd, PeekIterator cmdLine) {
		try {
			_field.set(grp, _converter.convert(cmd, cmdLine));
		} catch (IllegalArgumentException|IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String toHelpString() {
		return String.format("%s: %s", _name, _description);
	}
}