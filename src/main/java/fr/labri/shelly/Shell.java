package fr.labri.shelly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Iterator;

import fr.labri.shelly.ShellyException.EOLException;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.BasicExecutor;
import fr.labri.shelly.impl.Environ;
import fr.labri.shelly.impl.ModelUtil;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.PeekIterator;

public class Shell extends BasicExecutor {
	final Group<Class<?>, Member> _root;

	interface ShellAdapter {
		String prompt();
		Iterator<String> parseLine(String line);
		void catchBlock(Exception e) throws Exception;
		String readLine() throws IOException;
		void printResult(Object result) throws IOException;
	}

	public Shell(Recognizer parser, Group<Class<?>, Member> createGroup) {
		super(parser);
		_root = createGroup;
	}
	
	@Override
	public ExecutorMode getMode() {
		return ExecutorMode.INTERACTIVE;
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

	public void loop(InputStream inputStream) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		loop(inputStream, new ShellAdapter() {
			String prompt = String.format("%s> ", _root.getID());

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

	public void loop(InputStream in, ShellAdapter adapter) throws Exception {
		String line;
		do {
			System.out.print(adapter.prompt());
			System.out.flush();
			line = adapter.readLine();
			if (line != null)
				try {
					// Object result =
					new BasicExecutor.CommandExecutor(new PeekIterator<String>(adapter.parseLine(line)), new Environ()).execute(getRoot());;
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
			String prompt = String.format("%s> ", _root.getID());

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
