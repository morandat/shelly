package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import fr.labri.shelly.Action;
import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.Item;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.impl.Visitor.ActionVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	final PeekIterator<String> _cmdline;
	final Parser _parser;

	public Executor(Parser parser, PeekIterator<String> cmdline) {
		_parser = parser;
		_cmdline = cmdline;
	}

	public static void execute(Parser parser, Group<Class<?>, Member> start, final PeekIterator<String> cmdline) {
		Executor executor = new Executor(parser, cmdline); // TODO a method
		Object ctx = executor.fillOptions(start, start.instantiateObject(null));
		Action<Class<?>, Member> cmd = start;
		Action<Class<?>, Member> last = cmd;

		while ((cmd = Shell.findAction(last = cmd, parser, executor.peek())) != null)
			ctx = executor.executeCommand(executor._cmdline.next(), cmd, ctx);
		if (last instanceof Group)
			executor.executeDefault((Group<Class<?>, Member>) last, ctx);
	}

	public void executeDefault(Group<Class<?>, Member> subCmd, Object parent) {
		Action<Class<?>, Member> dflt = getDefault(subCmd);
		if (dflt != null)
			executeCommand(null, dflt, parent);
		else
			error(subCmd, parent);
	}
	
	@SuppressWarnings("unchecked")
	public Action<Class<?>, Member> getDefault(Group<Class<?>, Member> grp) {
		try {
			Visitor<Class<?>, Member> v = new ActionVisitor<Class<?>, Member>() {
				@Override
				public void visit(Command<Class<?>, Member> grp) {
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
			arr.add(_cmdline.next());
		try {
			found.invoke(parent, new RuntimeException("Command not found"), arr.toArray(new String[arr.size()]));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
	}

	public Object executeCommand(String txt, Action<Class<?>, Member> cmd, Object parent) {
		parent = cmd.createContext(parent);
		parent = fillOptions(cmd, parent);
		cmd.apply(parent, txt, this);
		return parent;
	}

	private Object fillOptions(Action<Class<?>, Member> subCmd, Object parent) {
		OptionParserVisitor visitor = new OptionParserVisitor();
		while (visitor.setOption(subCmd, parent))
			;
		return parent;
	}

	class OptionParserVisitor extends OptionVisitor<Class<?>, Member> {
		Object receive;

		@Override
		public void visit(Item<Class<?>, Member> item) {
			receive = null;
		}

		public void visit(Option<Class<?>, Member> opt) {
			if (_parser.isValid(_cmdline.peek(), opt)) {
				throw new FoundOption(opt);
			}
		}

		@Override
		public void visit(Context<Class<?>, Member> grp) {
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
				e.opt.apply(receive, _cmdline.next(), Executor.this);
				return true;
			}
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
}
