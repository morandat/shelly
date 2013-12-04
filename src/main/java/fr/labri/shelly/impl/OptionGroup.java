package fr.labri.shelly.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import fr.labri.shelly.Command;
import fr.labri.shelly.Option;
import fr.labri.shelly.impl.PeekIterator;

public class OptionGroup implements fr.labri.shelly.OptionGroup {
	final Constructor<?> _ctor;
	final List<Option> options = new ArrayList<Option>();
	final List<Command> commands = new ArrayList<Command>();

	public OptionGroup(Class<?> clazz) {
		Constructor<?> ctor;
		try {
			ctor = clazz.getConstructor();
			_ctor = ctor;
		} catch (NoSuchMethodException|SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Object newGroup() {
		try {
			return _ctor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void addOption(Option option) {
		options.add(option);
	}
	public void addCommand(Command cmd) {
		commands.add(cmd);
	}
	
	public Object fillOptions(PeekIterator cmdLine) {
		boolean ok = true;
		Object grp = newGroup();
		while (ok) {
			String text = cmdLine.peek();
			ok = fillOption(grp, text, cmdLine);
		}
		return grp;
	}

	protected boolean fillOption(Object grp, String text, PeekIterator cmdLine) {
		if ("--".equals(text)) {
			cmdLine.next();
			return true;
		}

		for (Option opt : options)
			if (opt.isValid(text)) {
				cmdLine.next();
				opt.apply(grp, text, cmdLine);
				return true;
			}

		return false;
	}

	public void execute(PeekIterator cmdLine) {
		String cmdText = cmdLine.peek();
		for (Command cmd : commands) {
			if ((cmd = cmd.isValid(cmdText)) != null) {
				cmdLine.next();
				cmd.parse(this, cmdText, cmdLine);
				return;
			}
		}
	}
}
