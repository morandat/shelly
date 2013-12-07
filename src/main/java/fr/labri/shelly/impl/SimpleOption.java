package fr.labri.shelly.impl;

import java.lang.reflect.Field;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Context;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Visitor;

class SimpleOption implements Option {
	final String _id;
	final Context _parent;
	final String _description = "No description";
	final Field _field;
	final Converter<?> _converter;

	SimpleOption(ConverterFactory factory, Context parent, String name, Field field) {
		_id = name;
		_field = field;
		_parent = parent;
		_converter = factory.getConverter(field.getType(), field);
	}

	public ShellyItem isValid(String str) {
		return str.equals("--" + _id) ? this : null;
	}

	public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
		try {
			_field.set(grp, _converter.convert(cmd, cmdLine));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public String[] getHelpString() {
		return new String[] { _id, _description };
	}

	@Override
	public void parse(Object parent, String cmdText, PeekIterator<String> cmdLine) {
	}

	@Override
	public void visit_all(Visitor visitor) {
	}
	

	public String getID() {
		return _id;
	}
	
	@Override
	public ShellyItem getParent() {
		return _parent;
	}
}