package fr.labri.shelly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.ModelBuilder;
import fr.labri.shelly.impl.Executor;
import fr.labri.shelly.impl.Visitor;
import fr.labri.shelly.impl.PeekIterator;
import fr.labri.shelly.impl.Visitor.FoundCommand;
import fr.labri.shelly.impl.Visitor.OptionVisitor;
import fr.labri.shelly.impl.Visitor.FoundOption;

public class Shell {
	Group<Class<?>, Member> grp;

	private Shell(Group<Class<?>, Member> createGroup) {
		grp = createGroup;
	}

	static public Shell createShell(ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return new Shell(factory.createModel(clazz));
	}

	static public Shell createShell(Class<?> clazz) {
		return createShell(new ModelBuilder.Executable(), clazz);
	}

	public Group<Class<?>, Member> getRoot() {
		return grp;
	}

	void addCommand(Command<Class<?>, Member> cmd) {
		addCommand(getRoot(), cmd);
	}

	static void addCommand(Composite<Class<?>, Member> group, Command<Class<?>, Member> cmd) {
		group.addCommand(cmd);
	}

	static void addCommand(Composite<Class<?>, Member> group, Composite<Class<?>, Member> cmd) {
		group.addCommand(cmd);
	}

	void addOption(Option<Class<?>, Member> opt) {
		addOption(getRoot(), opt);
	}

	static void addOption(Composite<Class<?>, Member> group, Option<Class<?>, Member> opt) {
		group.addOption(opt);
	}

	public void printHelp(PrintStream out) {
		HelpFactory.printHelp(getRoot(), out);
	}

	final public void parseCommandLine(String[] cmds) {
		parseCommandLine(Arrays.asList(cmds));
	}

	final public void parseCommandLine(Collection<String> cmds) {
		parse(cmds.iterator());
	}

	final public void parse(Iterator<String> cmdLine) {
		Executor.execute(grp, new PeekIterator<>(cmdLine));
	}

	public void loop(InputStream inputStream) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		loop(inputStream, new ShellAdapter() {
			String prompt = String.format("%s> ", grp.getID());

			@Override
			public String prompt() {
				return prompt;
			}

			@Override
			public String readLine() throws IOException {
				return in.readLine();
			}

			@Override
			public void catchBlock(Exception e) throws Exception {
				System.err.println("Error found " + e.getCause());
				e.printStackTrace(System.err);
			}

			@Override
			public void printResult(Object result) throws IOException {
			}
		});
	}

	public void loop(InputStream in, ShellAdapter adapter) throws Exception {
		String line;
		do {
			System.out.println(adapter.prompt());
			System.out.flush();
			line = adapter.readLine();
			if (line != null)
				try {
					// Object result =
					parseCommandLine(line.split(" "));
					// adapter.printResult(result);
				} catch (RuntimeException e) {
					adapter.catchBlock(e);
				} catch (Exception e) {
					adapter.catchBlock(e);
				}
		} while (line != null);
	}

	public Action<Class<?>, Member> find_command(final String cmd) {
		return find_command(grp, cmd);
	}

	public Option<Class<?>, Member> find_option(final String cmd) {
		return find_option(grp, cmd);
	}

	public static Action<Class<?>, Member> findAction(Action<Class<?>, Member> start, final String cmd) {
		if (start instanceof Group) {
			return find_command((Group<Class<?>, Member>) start, cmd);
		}
		return null;
	}
	
	public static Action<Class<?>, Member> find_command(Group<Class<?>, Member> start, final String cmd) {
		try {
			Visitor<Class<?>, Member> v = new Visitor.CommandVisitor<Class<?>, Member>() {
				@Override
				public void visit(Action<Class<?>, Member> grp) {
					if (grp.isValid(cmd)) {
						throw new Visitor.FoundCommand(grp);
					}
				}
			};
			start.visit_commands(v);
		} catch (Visitor.FoundCommand e) {
			return e.cmd;
		}
		return null;
	}
	
	public static Group<Class<?>, Member> find_group(Item<Class<?>, Member> start) {
		try {
		start.accept(new Visitor<Class<?>, Member>() {
			@Override
			public void visit(Item<Class<?>, Member> i) {
				visit_parent(i);
			}
			@Override
			public void visit(Group<Class<?>, Member> cmdGroup) {
				throw new FoundCommand(cmdGroup);
			}
		});
		} catch (FoundCommand e) {
			return (Group<Class<?>, Member>)e.cmd;
		}
		return null; 
	}
	
	static public Option<Class<?>, Member> find_option(Action<Class<?>, Member> start, final String cmd) {
		try {
			if (start instanceof Group) {
				Visitor<Class<?>, Member> v = new OptionVisitor<Class<?>, Member>() {
					public void visit(Option<Class<?>, Member> option) {
						if (option.getID().equals(cmd))
							throw new FoundOption(option);
					};
				};
				((Group<Class<?>, Member>) start).visit_options(v);
			}
		} catch (FoundOption e) {
			return e.opt;
		}
		return null;
	}

	interface ShellAdapter {
		String prompt();
		void catchBlock(Exception e) throws Exception;
		String readLine() throws IOException;
		void printResult(Object result) throws IOException;
	}
}
