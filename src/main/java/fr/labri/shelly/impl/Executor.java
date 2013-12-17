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
import fr.labri.shelly.Parser;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.Visitor.ActionVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;
import fr.labri.shelly.impl.Visitor.FoundOption;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	PeekIterator<String> _cmdline;
	final Parser _parser;
	final ExecutorMode _mode = ExecutorMode.BATCH;

	public Executor(Parser parser) {
		_parser = parser;
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start){
		execute(cmdline, start, new Environ());
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start, Environ environ){
		_cmdline = cmdline;
		Action<Class<?>, Member> cmd = start;
		Action<Class<?>, Member> last = cmd;
		
		start.instantiateObject(environ);
		fillOptions(start, environ);
		while (cmd != null && _cmdline.hasNext())
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

	private void fillOptions(Action<Class<?>, Member> subCmd, Environ environ) {
		if(!_cmdline.hasNext()) return;
		
		String peek = peek();
		if(_parser.stopOptionParsing(peek)) {
			next(); // consume token and stop parsing
//		} else if( _parser.isLongOption(peek)) {
//			// TODO implement short	
//			OptionParserVisitor visitor = new OptionParserVisitor() {
//				public void visit(Option<Class<?>, Member> opt) {
//					if (!_mode.isIgnored(opt))
//						if (_parser.isValidShortOption(letter, opt)) {
//							throw new FoundOption(opt);
//						}
//				}
//
//				@Override
//				public void setValue(Option<?, ?> opt) {
//					
//					opt.executeAction(receive, _cmdline.next(), Executor.this);					
//				}
//			};
//			while (visitor.setOption(subCmd, parent))
//				;
		} else {
			
			Option<Class<?>, Member> option;
			while (_cmdline.hasNext() && _parser.isLongOption(peek) && (option = find_option(subCmd)) != null)
				option.executeAction(environ.fetch(option), next(), this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Option<Class<?>, Member> find_option(Action<Class<?>, Member> cmd) {
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
				if(_parser.strictOptions())
					visit((Composite<Class<?>, Member>) grp);
			}
		});
		} catch (FoundOption e) {
			return (Option<Class<?>, Member>) e.opt;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Action<Class<?>, Member> findAction(Action<Class<?>, Member> action, final Parser parser, final String cmd) {
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

	public PeekIterator<String> getCommandLine() {
		return _cmdline;
	}

	public Parser getParser() {
		return _parser;
	}

	public String peek() {
		return _cmdline.peek();
	}
	public String next() {
		return _cmdline.next();
	}

}
