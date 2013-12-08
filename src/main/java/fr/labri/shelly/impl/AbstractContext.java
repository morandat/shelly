package fr.labri.shelly.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.labri.shelly.Context;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Visitor;

public abstract class AbstractContext implements Context {

	protected final String _id;
	protected final fr.labri.shelly.Context _parent;

	protected final Class<?> _clazz;
	final Field _superThis;
	final Constructor<?> _ctor;

	private final List<Option> options = new ArrayList<Option>();
	private final List<ShellyItem> commands = new ArrayList<ShellyItem>();

	public AbstractContext(Context parent, String name, Class<?> clazz) {
		_parent = parent;
		_id = name;
		_clazz = clazz;
		
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
	public Context getParent() {
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

	@Override
	public Iterable<ShellyItem> getItems() {
		return commands;
	}
	
	@Override
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

	@Override
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
	
	static Context getContext(String name, Context parent, Class<?> clazz, final ContextAdapter adapter) {
		return new AbstractContext(parent, name, clazz) {
		};
	}

	public interface ContextAdapter {
	}
}