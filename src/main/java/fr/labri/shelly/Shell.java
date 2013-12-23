package fr.labri.shelly;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Iterator;

import fr.labri.shelly.Executor.BasicExecutor;
import fr.labri.shelly.ShellyException.EOLException;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
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

		Environ newEnv();

		Group<Class<?>, Member> getRoot();

		void finalize(CommandExecutor executor, Action<Class<?>, Member> last);
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
		loop(inputStream, new SimpleShellAdapter(in));
	}

	public void loop(InputStream in, final ShellAdapter adapter) throws Exception {
		String line;
		try {
			do {
				System.out.print(adapter.prompt());
				System.out.flush();
				try {
					line = adapter.readLine();
					if (line != null) {
						Environ env = adapter.newEnv();
						Group<Class<?>, Member> root = adapter.getRoot();
						// Object result =
						new BasicExecutor.CommandExecutor(new PeekIterator<String>(adapter.parseLine(line)), env) {
							protected void finalize(Action<Class<?>, Member> last) {
								adapter.finalize(this, last);
							}
						}.execute(root);
						// adapter.printResult(result);
					}
				} catch (RuntimeException e) {
					adapter.catchBlock(e);
				} catch (Exception e) {
					adapter.catchBlock(e);
				}
			} while (true);
		} catch (EOFException e1) {
		}
	}

	class SimpleShellAdapter implements ShellAdapter {
			String prompt = String.format("%s> ", _root.getID());
			private BufferedReader in;

			SimpleShellAdapter(BufferedReader in) {
				this.in = in;
			}

			
			@Override
			public String readLine() throws IOException {
				return in.readLine();
			}
			@Override
			public String prompt() {
				return prompt;
			}
			@Override
			public void catchBlock(Exception e) throws Exception {
				if (e instanceof EOLException)
					return;
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace(System.err);
			}

			@Override
			public Iterator<String> parseLine(String line) {
				Iterator<String> a = Arrays.asList(line.split("\\s")).iterator();
				return a;
			}

			@Override
			public void printResult(Object result) throws IOException {
			}


			@Override
			public Environ newEnv() {
				return new Environ();
			}

			@Override
			public Group<Class<?>, Member> getRoot() {
				return this.getRoot();
			}
			@Override
			public void finalize(CommandExecutor executor, Action<Class<?>, Member> last) {
				if (last instanceof Group)
					executor.executeDefault((Group<Class<?>, Member>) last);
			}
	}
	public Action<Class<?>, Member> findAction(Recognizer parser, String cmd) {
		return ModelUtil.findAction(getRoot(), parser, cmd);
	}

	public Option<Class<?>, Member> findOption(Recognizer parser, String cmd) {
		return ModelUtil.findOption(getRoot(), parser, cmd);
	}

	public class MultiLevelShellAdapter extends SimpleShellAdapter {
		final Environ env = new Environ();
		Group<Class<?>, Member> current = Shell.this.getRoot();

		public MultiLevelShellAdapter(BufferedReader in) {
			super(in);
		}
		
		@Override
		public String prompt() {
			return String.format("%s> ", current.getID());
		}
		
		@Override
		public String readLine() throws IOException {
			String line = super.readLine();
			if ("".equals(line) && env.size() > 0)
				throw new EOLException();
			return line;
		}
		@Override
		public void catchBlock(Exception e) throws Exception {
			current = env.drop();
			super.catchBlock(e);
		}
		@Override
		public Environ newEnv() {
			return env;
		}
		
		@Override
		public Group<Class<?>, Member> getRoot() {
			return current;
		}
		
		@Override
		public void finalize(CommandExecutor executor, Action<Class<?>, Member> last) {
			current = env.drop();
		}
	}
}
