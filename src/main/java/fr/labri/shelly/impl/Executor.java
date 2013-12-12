package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	PeekIterator<String> _cmdline;

	public Executor(PeekIterator<String> cmdline) {
		_cmdline = cmdline;
	}

	public static void execute(Group<Class<?>, Member> start, final PeekIterator<String> cmdline) {
		Executor executor = new Executor(cmdline); // TODO a method
		Object ctx = executor.fillOptions(start, start.newGroup(null));
		Command<Class<?>, Member> cmd = start;
		Command<Class<?>, Member> last = cmd;

		while ((cmd = Shell.find_command(last = cmd, executor._cmdline.peek())) != null)
			ctx = executor.executeCommand(executor._cmdline.next(), cmd, ctx);
		if (last instanceof Group)
			executor.executeDefault((Group<Class<?>, Member>) last, ctx);
	}

	private void executeDefault(Group<Class<?>, Member> subCmd, Object parent) {
		Command<Class<?>, Member> dflt = subCmd.getDefault();
		if (dflt != null)
			executeCommand(null, dflt, parent);
		else
			error(subCmd, parent);
	}

	private void error(Context<Class<?>, Member> grp, Object parent) {
		Class<?> c = grp.getAssociatedElement();
		Method found = null;
		for(Method m: c.getDeclaredMethods())
			if(m.isAnnotationPresent(Error.class)) {
				found = m;
				break;
			}
		if(found != null)
			callError(grp, found, parent);
		else 
			if(grp.getParent() != null)
				error(grp.getParent(), grp.getEnclosing(parent));
	}
	

	private void callError(Context<Class<?>, Member> grp, Method found, Object parent) {
		ArrayList<String> arr = new ArrayList<String>();
		while(_cmdline.hasNext())
			arr.add(_cmdline.next());
		try {
			found.invoke(parent, new RuntimeException("Command not found"), arr.toArray(new String[arr.size()]));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
	}

	private Object executeCommand(String txt, Command<Class<?>, Member> subCmd, Object parent) {
		parent = subCmd.createContext(parent);
		parent = fillOptions(subCmd, parent);
		subCmd.apply(parent, txt, _cmdline);
		return parent;
	}

	private Object fillOptions(Command<Class<?>, Member> subCmd, Object parent) {
		OptionParserVisitor visitor = new OptionParserVisitor();
		while (visitor.find_option(subCmd, parent))
			;
		return parent;
	}

	class OptionParserVisitor extends OptionVisitor<Class<?>, Member> {
		Object receive;

		@Override
		public void visit(ShellyItem<Class<?>, Member> item) {
			receive = null;
		}

		public void visit(Option<Class<?>, Member> opt) {
			if (opt.isValid(_cmdline.peek())) {
				throw new FoundOption(opt);
			}
		}

		@Override
		public void visit(Context<Class<?>, Member> grp) {
			grp.visit_options(this);

			Context<Class<?>, Member> p = grp.getParent();
			if (p != null) {
				receive = grp.getEnclosing(receive);
				p.accept(this);
			}
		}

		public boolean find_option(Command<Class<?>, Member> cmd, Object group) {
			receive = group;
			try {
				visit_options(cmd);
				return false;
			} catch (FoundOption e) {
				e.opt.apply(receive, _cmdline.next(), _cmdline);
				return true;
			}
		}

	}
}
