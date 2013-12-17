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

import fr.labri.shelly.ShellyException.EOLException;
import fr.labri.shelly.impl.ModelUtil;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.Executor;
import fr.labri.shelly.impl.PeekIterator;

public class Shell {
	Group<Class<?>, Member> _grp;

	interface ShellAdapter {
		String prompt();
		Iterator<String> parseLine(String line);
		void catchBlock(Exception e) throws Exception;
		String readLine() throws IOException;
		void printResult(Object result) throws IOException;
	}

	Shell(Group<Class<?>, Member> createGroup) {
		_grp = createGroup;
	}

	public Group<Class<?>, Member> getRoot() {
		return _grp;
	}

	void addItem(Command<Class<?>, Member> cmd) {
		getRoot().addItem(cmd);
	}

	public void printHelp(PrintStream out) {
		HelpFactory.printHelp(getRoot(), out);
	}

	final public void parseCommandLine(String[] args) {
		parseCommandLine(args, Recognizer.Java);
	}

	final public void parseCommandLine(String[] args, Recognizer parser) {
		parseCommandLine(Arrays.asList(args), parser);
	}

	final public void parseCommandLine(Collection<String> args, Recognizer parser) {
		parseCommandLine(args.iterator(), parser);
	}

	final public void parseCommandLine(Iterator<String> args, Executor executor) {
		executor.execute(new PeekIterator<>(args), getRoot());
	}

	final public void parseCommandLine(Iterator<String> args, Recognizer parser) {
		parseCommandLine(args, new Executor(parser));
	}

	public void loop(InputStream inputStream, Recognizer parser) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		loop(inputStream, parser, new ShellAdapter() {
			String prompt = String.format("%s> ", _grp.getID());

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
				if (e instanceof EOLException)
					return;
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

	public void loop(InputStream in, Recognizer model, ShellAdapter adapter) throws Exception {
		String line;
		do {
			System.out.print(adapter.prompt());
			System.out.flush();
			line = adapter.readLine();
			if (line != null)
				try {
					// Object result =
					parseCommandLine(adapter.parseLine(line), model);
					// adapter.printResult(result);
				} catch (RuntimeException e) {
					adapter.catchBlock(e);
				} catch (Exception e) {
					adapter.catchBlock(e);
				}
		} while (line != null);
	}

	public Action<Class<?>, Member> findAction(Recognizer parser, String cmd) {
		return ModelUtil.findAction(getRoot(), parser, cmd);
	}

	public Option<Class<?>, Member> findOption(Recognizer parser, String cmd) {
		return ModelUtil.findOption(getRoot(), parser, cmd);
	}

	ShellAdapter getMultiLevelShell(final BufferedReader in) {
		return new ShellAdapter() {
			String prompt = String.format("%s> ", _grp.getID());

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
				if (e instanceof EOLException)
					return;
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
		};
	}
}
