package fr.labri.shelly;

import java.io.PrintStream;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.BasicExecutor;
import fr.labri.shelly.impl.Environ;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.PeekIterator;

public class CommandLine extends BasicExecutor{
	final Group<Class<?>, Member> _root;

	public CommandLine(Recognizer parser, Group<Class<?>, Member> createGroup) {
		super(parser);
		_root = createGroup;
	}

	@Override
	public ExecutorMode getMode() {
		return ExecutorMode.BATCH;
	}

	void addItem(Command<Class<?>, Member> cmd) {
		getRoot().addItem(cmd);
	}

	public Group<Class<?>, Member> getRoot() {
		return _root;
	}
	
	public void printHelp(PrintStream out) {
		HelpFactory.printHelp(getRoot(), out);
	}
	
	public void parseCommandLine(String args[]) {
		parseCommandLine(Arrays.asList(args));
	}

	public void parseCommandLine(Collection<String> args) {
		parseCommandLine(args.iterator());
	}
	
	public void parseCommandLine(Iterator<String> args) {
		new CommandExecutor(new PeekIterator<String>(args), new Environ());
	}
}
