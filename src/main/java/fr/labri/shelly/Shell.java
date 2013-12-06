package fr.labri.shelly;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import fr.labri.shelly.impl.HelpHelper;
import fr.labri.shelly.impl.OptionGroup;
import fr.labri.shelly.impl.OptionGroupFactory;
import fr.labri.shelly.impl.PeekIterator;

public class Shell {
	OptionGroup grp;
	private Shell(OptionGroup createGroup) {
		grp = createGroup;
	}
	
	static public Shell createShell(OptionGroupFactory factory, Class<?> clazz) {
		return new Shell(factory.createModel(clazz));
	}
	static public Shell createShell(Class<?> clazz) {
		return createShell(OptionGroupFactory.DEFAULT, clazz);
	}
	public static void printHelp(Class<?> clazz) {
		 printHelp(OptionGroupFactory.DEFAULT, clazz);
	}
	
	public static void printHelp(OptionGroupFactory factory, Class<?> clazz) {
		OptionGroup grp = factory.createModel(clazz);
		HelpHelper.printHelp(grp);
	}
	
	final public void parseCommandLine(String[] cmds) {
		parseCommandLine(Arrays.asList(cmds));
	}

	final public void parseCommandLine(Collection<String> cmds) {
		parse(cmds.iterator());
	}
	
	final public void parse(Iterator<String> cmdLine) {
		grp.execute(null, new PeekIterator<>(cmdLine));
	}
}
