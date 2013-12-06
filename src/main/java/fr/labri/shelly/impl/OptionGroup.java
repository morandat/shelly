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
import fr.labri.shelly.impl.PeekIterator;

public class OptionGroup implements fr.labri.shelly.OptionGroup {
	final String _id;
	final Class<?> _clazz;
	final OptionGroup _parent;

	final Constructor<?> _ctor;
	final Field _superThis;
	
	final List<Option> options = new ArrayList<Option>();
	final List<ShellyItem> commands = new ArrayList<ShellyItem>();

	public OptionGroup(OptionGroup parent, String name, Class<?> clazz) {
		_clazz = clazz;
		_parent = parent;
		_id = name;
		try {
			Constructor<?> ctor;
			if (parent != null) {
				ctor = clazz.getConstructor(clazz.getEnclosingClass());
				_superThis = getSuperThisField(clazz);
			} else {
				ctor = clazz.getConstructor();
				_superThis = null;
			}
			_ctor = ctor;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	protected Object newGroup(Object parent) {
		try {
			if (_parent != null)
				return _ctor.newInstance(parent);
			else
				return _ctor.newInstance();
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

	public Object fillOptions(Object parent, PeekIterator<String> cmdLine) {
		boolean ok = true;
		Object grp = newGroup(parent);
		while (ok) {
			String text = cmdLine.peek();
			ok = fillOption(grp, text, cmdLine);
		}
		return grp;
	}

	protected boolean fillOption(Object grp, String text, PeekIterator<String> cmdLine) {
		if ("--".equals(text)) {
			cmdLine.next();
			return true;
		}

		for (Option opt : options)
			if (opt.isValid(text) != null) {
				cmdLine.next();
				opt.apply(grp, text, cmdLine);
				return true;
			}

		return (_parent == null) ? false : _parent.fillOption(getEnclosing(grp), text, cmdLine);
	}

	public void execute(Object parent, PeekIterator<String> cmdLine) {
		String cmdText = cmdLine.peek();
		for (ShellyItem item : commands) {
			if ((item = item.isValid(cmdText)) != null) {
				cmdLine.next();
				item.parse(parent, cmdText, cmdLine);
				return;
			}
		}
	}

	@Override
	public ShellyItem isValid(String str) {
		for (ShellyItem item : commands)
			if ((item = item.isValid(str)) != null)
				return item;
		return null;
	}

	Object getEnclosing(Object obj) {
		try {
			return _superThis.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return new RuntimeException(e);
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
	public fr.labri.shelly.OptionGroup getParent() {
		return _parent;
	}

	@Override
	public void apply(Object grp, String cmd, PeekIterator<String> cmdLine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parse(Object parent, String cmdText, PeekIterator<String> cmdLine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getID() {
		return _id;
	}
}
