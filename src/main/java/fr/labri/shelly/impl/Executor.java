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
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.Visitor.ActionVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	PeekIterator<String> _cmdline;
	final Parser _parser;
	final ExecutorMode _mode = ExecutorMode.BATCH;

	public Executor(Parser parser) {
		_parser = parser;
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start){
		execute(cmdline, start, null);
	}
	
	public void execute(PeekIterator<String> cmdline, Group<Class<?>, Member> start, Object parent){
		_cmdline = cmdline;
		Action<Class<?>, Member> cmd = start;
		Action<Class<?>, Member> last = cmd;
		
		Object ctx = start.instantiateObject(parent);
		fillOptions(start, ctx);
		while (cmd != null && _cmdline.hasNext())
			if ((cmd = findAction(last = cmd, _parser, peek())) != null)
				ctx = executeAction(next(), last = cmd, ctx);
		
		finalize(last, ctx);
	}

	protected void finalize(Action<Class<?>, Member> last, Object ctx) {
		if (last instanceof Group)
			executeDefault((Group<Class<?>, Member>) last, ctx);
	}
	
	public void executeDefault(Group<Class<?>, Member> subCmd, Object parent) {
		Action<Class<?>, Member> dflt = getDefault(subCmd);
		if (dflt != null)
			executeAction(null, dflt, parent);
		else
			error(subCmd, parent);
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

	public void error(Composite<Class<?>, Member> grp, Object receiv) {
		while(grp != null) {
			for(Method m : receiv.getClass().getDeclaredMethods())
				if(m.isAnnotationPresent(Error.class)) {
					callError(m, receiv);
					return;
				} else
					if(grp.getParent() != null)
						receiv = grp.getEnclosingObject(receiv);
		}
	}

	public void callError(Method found, Object parent) {
		ArrayList<String> arr = new ArrayList<String>();
		while (_cmdline.hasNext())
			arr.add(next());
		try {
			found.invoke(parent, new RuntimeException("Command not found"), arr.toArray(new String[arr.size()]));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
	}

	public Object executeAction(String txt, Action<Class<?>, Member> cmd, Object parent) {
		parent = createContext(cmd, parent);
		fillOptions(cmd, parent);
		cmd.executeAction(parent, txt, this);
		return parent;
	}

	private void fillOptions(Action<Class<?>, Member> subCmd, Object parent) {
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
			OptionSetterVisitor visitor = new OptionSetterVisitor() {
				public void visit(Option<Class<?>, Member> opt) {
					if (!_mode.isIgnored(opt))
						if (_parser.isLongOptionValid(peek(), opt)) {
							throw new FoundOption(opt);
						}
				}

				@Override
				public void setValue(Option<?, ?> opt) {
					opt.executeAction(receive, next(), Executor.this);					
				}
			};
			while (_cmdline.hasNext() && _parser.isLongOption(peek) && visitor.setOption(subCmd, parent))
			;
		}
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

	abstract class OptionSetterVisitor extends OptionVisitor<Class<?>, Member> {
		Object receive;

		@Override
		public void visit(Item<Class<?>, Member> item) {
			receive = null;
		}

		abstract public void visit(Option<Class<?>, Member> opt);

		@Override
		public void visit(Group<Class<?>, Member> grp) {
			if(_parser.strictOptions())
				visit((Composite<Class<?>, Member>) grp);
		}

		@Override
		public void visit(Composite<Class<?>, Member> grp) {
			visit_options(grp);

			Composite<Class<?>, Member> p = grp.getParent();
			if (p != null) {
				receive = grp.getEnclosingObject(receive);
				p.accept(this);
			}
		}

		public boolean setOption(Action<Class<?>, Member> cmd, Object group) {
			receive = group;
			try {
				visit_options(cmd);
				return false;
			} catch (FoundOption e) {
				if (e.opt == null)
					return false;
				setValue(e.opt);
				return true;
			}
		}

		abstract public void setValue(Option<?, ?> opt);
	}
	
	private Object createContext(Action<Class<?>, Member> cmd, Object parent) {
		InstVisitor v = new InstVisitor(parent);
		cmd.startVisit(v);
		return v.group;
	}

	static class InstVisitor extends fr.labri.shelly.impl.Visitor.ParentVisitor<Class<?>, Member> {
		private Object group;
		public InstVisitor(Object parent) {
			group = parent;
		}

		@Override
		public void visit(Group<Class<?>, Member> cmdGroup) {
		}

		@Override
		public void visit(Composite<Class<?>, Member> ctx) {
			visit((Item<Class<?>, Member>)ctx);
			group = ctx.instantiateObject(group);
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
