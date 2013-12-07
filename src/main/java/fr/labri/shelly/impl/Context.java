package fr.labri.shelly.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Visitor;

public class Context implements fr.labri.shelly.Context {
	final String _id;
	final Class<?> _clazz;
	final fr.labri.shelly.Context _parent;

	final Constructor<?> _ctor;
	final Field _superThis;

	private final List<Option> options = new ArrayList<Option>();
	final List<ShellyItem> commands = new ArrayList<ShellyItem>();

	public Context(fr.labri.shelly.Context parent, String name, Class<?> clazz) {
		_clazz = clazz;
		_parent = parent;
		_id = name;
		try {
			Constructor<?> ctor;
			if (clazz.getEnclosingClass() != null) {
				ctor = clazz.getConstructor(clazz.getEnclosingClass());
				_superThis = getSuperThisField(clazz);
				_superThis.setAccessible(true);
			} else {
				ctor = clazz.getConstructor();
				_superThis = null;
			}
			_ctor = ctor;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public Object newGroup(Object parent) {
		try {
			if (_superThis != null) {
				return _ctor.newInstance(parent);
			} else {
				return _ctor.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void addOption(Option option) {
		options.add(option);
	}

	public void addCommand(ShellyItem cmd) {
		commands.add(cmd);
	}

	@Override
	public boolean isValid(String str) {
		return getID().equals(str);
	}

	public Object getEnclosing(Object obj) {
		try {
			return _superThis.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Field getSuperThisField(Class<?> c) {
		Field[] fields = c.getDeclaredFields();
		for (Field f : fields)
			if (f.getName().startsWith("this$"))
				return f;
		throw new RuntimeException("This class has no enclosing class.\n" + Arrays.toString(fields));
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public void visit_options(Visitor visitor) {
		for (Option cmd : options)
			cmd.accept(visitor);
	}

	public void visit_commands(Visitor visitor) {
		for (ShellyItem cmd : commands)
			cmd.accept(visitor);
	}

	@Override
	public void visit_all(Visitor visitor) {
		visit_options(visitor);
		visit_commands(visitor);
	}

	@Override
	public Class<?> getAssociatedClass() {
		return _clazz;
	}

	@Override
	public fr.labri.shelly.Context getParent() {
		return _parent;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public Iterable<Option> getOptions() {
		return options;
	}
}
