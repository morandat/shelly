package fr.labri.shelly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.Environ;
import fr.labri.shelly.impl.PeekIterator;
import fr.labri.shelly.impl.VisitorAdapter;
import fr.labri.shelly.impl.VisitorAdapter.ActionVisitor;
import fr.labri.shelly.impl.VisitorAdapter.FoundCommand;
import fr.labri.shelly.impl.VisitorAdapter.FoundOption;
import fr.labri.shelly.impl.VisitorAdapter.OptionVisitor;

public interface Executor {

	public abstract Recognizer getRecognizer();
	public abstract ExecutorMode getMode();
	
	public abstract PeekIterator<String> getCommandLine();
	
	public abstract void execute(Group<Class<?>, Member> start);
	public abstract void error(Composite<Class<?>, Member> grp);

	static public abstract class BasicExecutor {
		final Recognizer _recognizer;

		public BasicExecutor(Recognizer parser) {
			_recognizer = parser;
		}

		public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start) {
			execute(cmdline, start, new Environ());
		}

		public Recognizer getRecognizer() {
			return _recognizer;
		}
		public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start, Environ environ) {
			new CommandExecutor(cmdline, environ).execute(start);
		}
		
		public abstract ExecutorMode getMode();

		protected class CommandExecutor implements Executor {
			final PeekIterator<String> _cmdline;
			final Environ _environ;
			
			public CommandExecutor(PeekIterator<String> cmdline, Environ environ) {
				_cmdline = cmdline;
				_environ = environ;
			}
			
			public PeekIterator<String> getCommandLine() {
				return _cmdline;
			}

			@Override
			public Recognizer getRecognizer() {
				return BasicExecutor.this.getRecognizer();
			}

			@Override
			public ExecutorMode getMode() {
				return BasicExecutor.this.getMode();
			}

			@Override
			public void execute(Group<Class<?>, Member> start) {
				Environ environ = _environ;
				PeekIterator<String> cmdline = _cmdline;
				Action<Class<?>, Member> cmd = start;
				Action<Class<?>, Member> last = cmd;

				environ.push(start, start.instantiateObject(environ.getLast()));
				fillOptions(start);
				while (cmd != null && cmdline.hasNext())
					if ((cmd = findAction(last = cmd)) != null)
						executeAction(cmdline.next(), last = cmd);

				finalize(last);
			}
			
			protected void finalize(Action<Class<?>, Member> last) {
				if (last instanceof Group)
					executeDefault((Group<Class<?>, Member>) last);
			}

			public void executeDefault(Group<Class<?>, Member> subCmd) {
				Action<Class<?>, Member> dflt = getDefault(subCmd);
				if (dflt != null)
					executeAction(null, dflt);
				else
					error(subCmd);
			}

			@SuppressWarnings("unchecked")
			public Action<Class<?>, Member> getDefault(Group<Class<?>, Member> grp) {
				try {
					VisitorAdapter<Class<?>, Member> v = new ActionVisitor<Class<?>, Member>() {
						@Override
						public void visit(Action<Class<?>, Member> grp) {
							if (grp.hasAnnotation(Default.class)) {
								throw new FoundCommand(grp);
							}
						}
					};
					grp.visit_all(v);
				} catch (FoundCommand e) {
					return (Action<Class<?>, Member>) e.cmd;
				}
				return null;
			}

			@Override
			public void error(Composite<Class<?>, Member> grp) {
				Error error;
				Environ environ = _environ;
				ExecutorMode mode = getMode();
				while (grp != null) {
					for (Method m : grp.getAssociatedElement().getDeclaredMethods())
						if ((error = m.getAnnotation(Error.class)) != null) {
							if (!mode.isIgnored(error.ignores())) {
								callError(m);
								return;
							}
						}
					environ.pop();
					grp = grp.getParent();
				}
			}

			public void callError(Method found) {
				PeekIterator<String> cmdline = _cmdline;
				ArrayList<String> arr = new ArrayList<String>();
				while (cmdline.hasNext())
					arr.add(cmdline.next());
				try {
					found.invoke(_environ.getLast(), new RuntimeException("Command not found"), arr.toArray(new String[arr.size()]));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			}

			public void executeAction(String txt, Action<Class<?>, Member> cmd) {
				Environ environ = _environ;
				createContext(cmd, environ);
				fillOptions(cmd);
				cmd.execute(environ.getLast(), txt, this);
			}

			private void fillOptions(Action<Class<?>, Member> action) {
				PeekIterator<String> cmdline = _cmdline;
				if (!cmdline.hasNext())
					return;

				Recognizer reco = _recognizer;
				Option<Class<?>, Member> option;
				while (cmdline.hasNext()) {
					String peek = cmdline.peek();
					if (reco.stopOptionParsing(peek)) {
						cmdline.next(); // consume token and stop parsing
						break;
					} else if (reco.isLongOption(peek) >= 0 && (option = findOption(action, peek)) != null) {
						option.execute(_environ.get(option), cmdline.next(), this);
					} else if (reco.isShortOption(peek) >= 0) {
						int i = 0;
						String sentinel = null;
						while(++i < peek.length() && (option = findOption(action, peek.charAt(i))) != null) {
							if ((i+1) < peek.length()) {
								sentinel = peek.substring(i + 1);
								getCommandLine().replace(sentinel);
							} else
								getCommandLine().next();
								
							option.execute(_environ.get(option), Character.toString(peek.charAt(i)), this);
							if(getCommandLine().peek() != sentinel)
								break;
						}
						if (i < peek.length() && getCommandLine().peek() == sentinel) {
							throw new ShellyException(String.format("Unknown short option: %c in %s", peek.charAt(i), action));
						}
					} else
						break;
				}
			}

			@SuppressWarnings("unchecked")
			public Option<Class<?>, Member> findOption(Action<Class<?>, Member> cmd, final String key) {
				try {
					cmd.startVisit(new OptionVisitor<Class<?>, Member>() {
						public void visit(Option<Class<?>, Member> opt) {
							if (!getMode().isIgnored(opt))
//								if (getRecognizer().isLongOptionValid(getCommandLine().peek(), opt) >= 0) {
								if (opt.isValidLongOption(getRecognizer(), key, 2) >= 0) { // FIXME
									throw new FoundOption(opt);
								}
						}

						@Override
						public void visit(Group<Class<?>, Member> grp) {
							if (!getRecognizer().strictOptions())
								visit((Composite<Class<?>, Member>) grp);
						}
					});
				} catch (FoundOption e) {
					return (Option<Class<?>, Member>) e.opt;
				}
				return null;
			}
			
			@SuppressWarnings("unchecked")
			public Option<Class<?>, Member> findOption(Action<Class<?>, Member> cmd, final char f) {
				try {
					cmd.startVisit(new OptionVisitor<Class<?>, Member>() {
						public void visit(Option<Class<?>, Member> opt) {
							if (!getMode().isIgnored(opt))
//								if (getRecognizer().isLongOptionValid(getCommandLine().peek(), opt) >= 0) {
								if (opt.isValidShortOption(getRecognizer(), f)) { // FIXME
									throw new FoundOption(opt);
								}
						}

						@Override
						public void visit(Group<Class<?>, Member> grp) {
							if (!getRecognizer().strictOptions())
								visit((Composite<Class<?>, Member>) grp);
						}
					});
				} catch (FoundOption e) {
					return (Option<Class<?>, Member>) e.opt;
				}
				return null;
			}

			@SuppressWarnings("unchecked")
			public Action<Class<?>, Member> findAction(Action<Class<?>, Member> action) {
				try {
					VisitorAdapter<Class<?>, Member> v = new VisitorAdapter.ActionVisitor<Class<?>, Member>() {
						private String peek = getCommandLine().peek();
						@Override
						public void visit(Action<Class<?>, Member> grp) {
							if (!getMode().isIgnored(grp))
								if (getRecognizer().isActionValid(peek, grp) >= 0) {
									throw new VisitorAdapter.FoundCommand(grp);
								}
						}
					};
					action.startVisit(v);
				} catch (VisitorAdapter.FoundCommand e) {
					return (Action<Class<?>, Member>) e.cmd;
				}
				return null;
			}

			private void createContext(Action<Class<?>, Member> cmd, Environ environ) {
				cmd.startVisit(new InstVisitor(environ));
			}

			class InstVisitor extends VisitorAdapter.ParentVisitor<Class<?>, Member> {
				
				public InstVisitor(Environ environ) {
				}
				
				@Override
				public void visit(Group<Class<?>, Member> cmdGroup) {
				}
				
				@Override
				public void visit(Composite<Class<?>, Member> ctx) {
					Environ environ = _environ;
					visit((Item<Class<?>, Member>) ctx);
					environ.push(ctx, ctx.instantiateObject(environ.getLast()));
				}
				
				public void startVisit(Group<Class<?>, Member> cmdGroup) {
					visit((Composite<Class<?>, Member>) cmdGroup);
				}
			}
		}
	}
}
