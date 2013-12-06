package fr.labri.shelly;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import fr.labri.shelly.impl.HelpHelper;
import fr.labri.shelly.impl.OptionGroup;
import fr.labri.shelly.impl.ModelFactory;
import fr.labri.shelly.impl.PeekIterator;

public class Shell {
	CommandGroup grp;
	
	private Shell(CommandGroup createGroup) {
		grp = createGroup;
	}
	
	static public Shell createShell(ModelFactory factory, Class<?> clazz) {
		return new Shell(factory.createModel(clazz));
	}
	static public Shell createShell(Class<?> clazz) {
		return createShell(ModelFactory.DEFAULT, clazz);
	}
	
	public CommandGroup getGroup() {
		return grp;
	}
	
	public static void printHelp(Class<?> clazz) {
		 printHelp(ModelFactory.DEFAULT, clazz);
	}
	
	public static void printHelp(ModelFactory factory, Class<?> clazz) {
		Shell shell = createShell(factory, clazz);
		HelpHelper.printHelp(shell);
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
