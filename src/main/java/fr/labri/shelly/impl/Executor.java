package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class Executor {
	PeekIterator<String> _cmdline;

	public Executor(PeekIterator<String> cmdline) {
		_cmdline = cmdline;
	}

	public static void execute(Group start, final PeekIterator<String> cmdline) {
		Executor executor = new Executor(cmdline);
		Object ctx = executor.fillOptions(start, start.newGroup(null));
		Command cmd = start;
		Command last = cmd;

		while ((cmd = Shell.find_command(last = cmd, executor._cmdline.peek())) != null)
			ctx = executor.executeCommand(executor._cmdline.next(), cmd, ctx);
		if (last instanceof Group)
			executor.executeDefault((Group) last, ctx);
	}

	private void executeDefault(Group subCmd, Object parent) {
		Command dflt = subCmd.getDefault();
		if (dflt != null)
			executeCommand(null, dflt, parent);
	}

	private Object executeCommand(String txt, Command subCmd, Object parent) {
		parent = subCmd.createContext(parent);
		parent = fillOptions(subCmd, parent);
		subCmd.apply(parent, txt, _cmdline);
		return parent;
	}

	private Object fillOptions(Command subCmd, Object parent) {
		OptionParserVisitor visitor = new OptionParserVisitor();
		while (visitor.find_option(subCmd, parent))
			;
		return parent;
	}


	class OptionParserVisitor extends OptionVisitor {
		Object receive;

		@Override
		public void visit(ShellyItem item) {
			receive = null;
		}

		public void visit(Option opt) {
			if (opt.isValid(_cmdline.peek())) {
				throw new FoundOption(opt);
			}
		}

		@Override
		public void visit(Context grp) {
			grp.visit_options(this);

			Context p = grp.getParent();
			if (p != null) {
				receive = grp.getEnclosing(receive);
				p.accept(this);
			}
		}

		public boolean find_option(Command cmd, Object group) {
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
