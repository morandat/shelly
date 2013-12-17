package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import fr.labri.shelly.Action;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Item;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.Visitor.ActionVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;
import fr.labri.shelly.impl.Visitor.FoundOption;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	PeekIterator<String> _cmdline;
	final Recognizer _parser;
	final ExecutorMode _mode;

	public Executor(Recognizer parser) {
		this(parser, ExecutorMode.BATCH);
	}
	public Executor(Recognizer parser, ExecutorMode mode) {
		_parser = parser;
		_mode = mode;
	}	
	private void setCmdLine(PeekIterator<String> cmdline) {
		_cmdline = cmdline;		
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start){
		execute(cmdline, start, new Environ());
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start, Environ environ){
		setCmdLine(cmdline);
		Action<Class<?>, Member> cmd = start;
		Action<Class<?>, Member> last = cmd;
		
		start.instantiateObject(environ);
		fillOptions(start, environ);
		while (cmd != null && hasNext())
			if ((cmd = findAction(last = cmd, _parser, peek())) != null)
				executeAction(next(), last = cmd, environ);
		
		finalize(last, environ);
	}

	protected void finalize(Action<Class<?>, Member> last, Environ environ) {
		if (last instanceof Group)
			executeDefault((Group<Class<?>, Member>) last, environ);
	}
	
	public void executeDefault(Group<Class<?>, Member> subCmd, Environ environ) {
		Action<Class<?>, Member> dflt = getDefault(subCmd);
		if (dflt != null)
			executeAction(null, dflt, environ);
		else
			error(subCmd, environ);
	}
	
	@SuppressWarnings("unchecked")
	public Action<Class<?>, Member> getDefault(Group<Class<?>, Member> grp) {
		try {
			Visitor<Class<?>, Member> v = new ActionVisitor<Class<?>, Member>() {
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

	public void error(Composite<Class<?>, Member> grp, Environ environ) {
		while(grp != null) {
			for(Method m : grp.getAssociatedElement().getDeclaredMethods())
				if(m.isAnnotationPresent(Error.class)) {
					callError(m, environ);
					return;
				}
			environ.pop();
			grp = grp.getParent();
		}
	}

	public void callError(Method found, Environ environ) {
		ArrayList<String> arr = new ArrayList<String>();
		while (_cmdline.hasNext())
			arr.add(next());
		try {
			found.invoke(environ.getLast(), new RuntimeException("Command not found"), arr.toArray(new String[arr.size()]));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
	}

	public void executeAction(String txt, Action<Class<?>, Member> cmd, Environ environ) {
		createContext(cmd, environ);
		fillOptions(cmd, environ);
		cmd.executeAction(environ.getLast(), txt, this);
	}

	private void fillOptions(Action<Class<?>, Member> action, Environ environ) {
		if(!_cmdline.hasNext()) return;
		
		String peek = peek();
		if(_parser.stopOptionParsing(peek)) {
			next(); // consume token and stop parsing
		} else { // TODO add short options
			Option<Class<?>, Member> option;
			while (hasNext() && _parser.isLongOption(peek) && (option = findOption(action)) != null)
				option.executeAction(environ.fetch(option), next(), this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Option<Class<?>, Member> findOption(Action<Class<?>, Member> cmd) {
		try {
		cmd.startVisit(new OptionVisitor<Class<?>, Member>() {
			public void visit(Option<Class<?>, Member> opt) {
				if (!_mode.isIgnored(opt))
					if (_parser.isLongOptionValid(peek(), opt)) {
						throw new FoundOption(opt);
					}
			}
			@Override
			public void visit(Group<Class<?>, Member> grp) {
				if(!_parser.strictOptions())
					visit((Composite<Class<?>, Member>) grp);
			}
		});
		} catch (FoundOption e) {
			return (Option<Class<?>, Member>) e.opt;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Action<Class<?>, Member> findAction(Action<Class<?>, Member> action, final Recognizer parser, final String cmd) {
		try {
			Visitor<Class<?>, Member> v = new Visitor.ActionVisitor<Class<?>, Member>() {
				@Override
				public void visit(Action<Class<?>, Member> grp) {
					if (!_mode.isIgnored(grp))
						if (parser.isActionValid(cmd, grp)) {
							throw new Visitor.FoundCommand(grp);
						}
				}
			};
			action.startVisit(v);
		} catch (Visitor.FoundCommand e) {
			return (Action<Class<?>, Member>) e.cmd;
		}
		return null;
	}

	private void createContext(Action<Class<?>, Member> cmd, Environ environ) {
		cmd.startVisit(new InstVisitor(environ));
	}

	public PeekIterator<String> getCommandLine() {
		return _cmdline;
	}

	public Recognizer getParser() {
		return _parser;
	}

	public boolean hasNext() {
		return _cmdline.hasNext();
	}

	public String peek() {
		return _cmdline.peek();
	}
	public String next() {
		return _cmdline.next();
	}

	static class InstVisitor extends fr.labri.shelly.impl.Visitor.ParentVisitor<Class<?>, Member> {
		private Environ _environ;
		public InstVisitor(Environ environ) {
			_environ = environ;
		}
		
		@Override
		public void visit(Group<Class<?>, Member> cmdGroup) {
		}
		
		@Override
		public void visit(Composite<Class<?>, Member> ctx) {
			visit((Item<Class<?>, Member>)ctx);
			ctx.instantiateObject(_environ);
		}
		
		public void startVisit(Group<Class<?>, Member> cmdGroup) {
			visit((Composite<Class<?>, Member>) cmdGroup);
		}
	}
}
