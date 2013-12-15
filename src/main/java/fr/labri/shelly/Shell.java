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

import fr.labri.shelly.impl.ExecutableModelFactory;
import fr.labri.shelly.impl.Parser;
import fr.labri.shelly.impl.ParserFactory;
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
		return createShell(new ExecutableModelFactory.Executable(), clazz);
	}

	public Group<Class<?>, Member> getRoot() {
		return grp;
	}

	void addItem(Command<Class<?>, Member> cmd) {
		getRoot().addItem(cmd);
	}

	public void printHelp(PrintStream out) {
		HelpFactory.printHelp(getRoot(), out);
	}


	final public void parseCommandLine(String[] cmds) {
		parseCommandLine(cmds, ParserFactory.Java);
	}
	
	final public void parseCommandLine(String[] cmds, Parser parser) {
		parseCommandLine(Arrays.asList(cmds), parser);
	}

	final public void parseCommandLine(Collection<String> cmds, Parser parser) {
		parse(cmds.iterator(), parser);
	}
	
	final public void parse(Iterator<String> cmdLine, Executor executor) {
		executor.execute(new PeekIterator<>(cmdLine), getRoot());
	}
	final public void parse(Iterator<String> cmdLine, Parser parser) {
		parse(cmdLine, new Executor(parser));
	}

	public void loop(InputStream inputStream, Parser model) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		loop(inputStream, model, new ShellAdapter() {
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

			@Override
			public Iterator<String> parseLine(String line) {
				Iterator<String> a = Arrays.asList(line.split("\\s")).iterator();
				return a;
			}

		});
	}

	public void loop(InputStream in, Parser model, ShellAdapter adapter) throws Exception {
		String line;
		do {
			System.out.print(adapter.prompt());
			System.out.flush();
			line = adapter.readLine();
			if (line != null)
				try {
					// Object result =
					parse(adapter.parseLine(line), model);
					// adapter.printResult(result);
				} catch (RuntimeException e) {
					adapter.catchBlock(e);
				} catch (Exception e) {
					adapter.catchBlock(e);
				}
		} while (line != null);
	}

	public Action<Class<?>, Member> find_command(Parser parser, String cmd) {
		return findAction(getRoot(), parser, cmd);
	}

	public Option<Class<?>, Member> find_option(Parser parser, String cmd) {
		return find_option(getRoot(), parser, cmd);
	}

	public static <C, M> Action<C, M> findAction(Action<C, M> start, Parser parser, final String cmd) {
		if (start instanceof Group) {
			return findAction((Group<C, M>) start, parser, cmd);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <C,M> Action<C, M> findAction(Group<C, M> start, final Parser parser, final String cmd) {
		try {
			Visitor<C, M> v = new Visitor.ActionVisitor<C, M>() {
				@Override
				public void visit(Action<C, M> grp) {
					if (parser.isValid(cmd, grp)) {
						throw new Visitor.FoundCommand(grp);
					}
				}
			};
			start.startVisit(v);
		} catch (Visitor.FoundCommand e) {
			return (Action<C, M>) e.cmd;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <C,M> Group<C, M> find_group(Item<C, M> start) {
		try {
		start.accept(new Visitor<C, M>() {
			@Override
			public void visit(Item<C, M> i) {
				visit_parent(i);
			}
			@Override
			public void visit(Group<C, M> cmdGroup) {
				throw new FoundCommand(cmdGroup);
			}
		});
		} catch (FoundCommand e) {
			return (Group<C, M>)e.cmd;
		}
		return null; 
	}
	
	@SuppressWarnings("unchecked")
	static public <C,M> Option<C, M> find_option(Action<C, M> start, final Parser parser, final String cmd) {
		try {
			if (start instanceof Group) {
				OptionVisitor<C, M> v = new OptionVisitor<C, M>() {
					public void visit(Option<C, M> option) {
						if (parser.isValid(cmd, option))
							throw new FoundOption(option);
					};
				};
				v.visit_options(start);
			}
		} catch (FoundOption e) {
			return (Option<C, M>) e.opt;
		}
		return null;
	}

	interface ShellAdapter {
		String prompt();
		Iterator<String> parseLine(String line);
		void catchBlock(Exception e) throws Exception;
		String readLine() throws IOException;
		void printResult(Object result) throws IOException;
	}
}
