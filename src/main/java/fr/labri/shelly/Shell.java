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
	
	Command find_command(String name) {
		
		return find_command(grp, name);
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
		Parser.execute(grp, new PeekIterator<>(cmdLine));
	}
	
	public void loop(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while((line = in.readLine()) != null)
		try {
			parseCommandLine(line.split(" "));
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static Command find_command(Command start, final String cmd) {
		try {
			if(start instanceof Group) {
				Visitor v = new Visitor.CommandVisitor() {
					@Override
					public void visit(Command grp) {
						if (grp.isValid(cmd)) {
							throw new Visitor.FoundCommand(grp);
						}
					}
				};
				((Group)start).visit_commands(v);
			}
		} catch (Visitor.FoundCommand e) {
			return e.cmd;
		}
		return null;
	}
}
