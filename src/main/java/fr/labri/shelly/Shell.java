package fr.labri.shelly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import fr.labri.shelly.impl.HelpHelper;
import fr.labri.shelly.impl.ModelFactory;
import fr.labri.shelly.impl.Parser;
import fr.labri.shelly.impl.Visitor;
import fr.labri.shelly.impl.PeekIterator;
import fr.labri.shelly.impl.Visitor.OptionVisitor;
import fr.labri.shelly.impl.Visitor.FoundOption;

public class Shell {
	Group grp;

	private Shell(Group createGroup) {
		grp = createGroup;
	}

	static public Shell createShell(ModelFactory factory, Class<?> clazz) {
		return new Shell(factory.createModel(clazz));
	}

	static public Shell createShell(Class<?> clazz) {
		return createShell(ModelFactory.DEFAULT, clazz);
	}

	public Group getGroup() {
		return grp;
	}
	public void printHelp() {
		HelpHelper.printHelp(this);
	}
	public static void printHelp(Class<?> clazz) {
		printHelp(ModelFactory.DEFAULT, clazz);
	}

	public static void printHelp(ModelFactory factory, Class<?> clazz) {
		createShell(factory, clazz).printHelp();
	}

	final public void parseCommandLine(String[] cmds) {
		parseCommandLine(Arrays.asList(cmds));
	}

	final public void parseCommandLine(Collection<String> cmds) {
		parse(cmds.iterator());
	}

	final public void parse(Iterator<String> cmdLine) {
		Parser.execute(grp, new PeekIterator<>(cmdLine));
	}

	public void loop(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = in.readLine()) != null) {
			try {
				parseCommandLine(line.split(" "));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public Command find_command(final String cmd) {
		return find_command(grp, cmd);
	}
	
	public Option find_option(final String cmd) {
		return find_option(grp, cmd);
	}
	
	public static Command find_command(Command start, final String cmd) {
		try {
			if (start instanceof Group) {
				Visitor v = new Visitor.CommandVisitor() {
					@Override
					public void visit(Command grp) {
						if (grp.isValid(cmd)) {
							throw new Visitor.FoundCommand(grp);
						}
					}
				};
				((Group) start).visit_commands(v);
			}
		} catch (Visitor.FoundCommand e) {
			return e.cmd;
		}
		return null;
	}
	
	static public Option find_option(Command start, final String cmd) {
		try {
			if (start instanceof Group) {
				Visitor v = new OptionVisitor() {
					public void visit(Option option) {
						if (option.getID().equals(cmd))
							throw new FoundOption(option);
					};
				};
				((Group) start).visit_options(v);
			}
		} catch (FoundOption e) {
			return e.opt;
		}
		return null;
	}
}
