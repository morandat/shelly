package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyItem;

public class Parser {
	PeekIterator<String> _cmdline;

	public Parser(PeekIterator<String> cmdline) {
		_cmdline = cmdline;
	}

	public static void execute(Group start, final PeekIterator<String> cmdline) {
		Parser parser = new Parser(cmdline);
		Object ctx = parser.fillOptions(start, start.newGroup(null));
		Command cmd = start;
		Command last = cmd;
		
		while((cmd = Shell.find_command(last = cmd, parser._cmdline.peek())) != null)
			ctx = parser.executeCommand(parser._cmdline.next(), cmd, ctx);
		if(last instanceof Group)
			parser.executeDefault((Group)last, ctx);
	}
	
	private void executeDefault(Group subCmd, Object parent) {
		Command dflt = subCmd.getDefault();
		if(dflt != null)
			executeCommand(null, dflt, parent);
	}
	
	private Object executeCommand(String txt, Command subCmd, Object parent) {
		parent = subCmd.createContext(parent);
		parent = fillOptions(subCmd, parent);
		subCmd.apply(parent, txt, _cmdline);
		return parent;
	}

	private Object fillOptions(Command subCmd, Object parent) {
		while (new OptionParserVisitor().visit_options(subCmd, parent))
			;
		return parent;
	}

	class OptionParserVisitor extends Visitor {
		Object receive;

		@Override
		public void visit(ShellyItem item) {
			receive = null;
		}

		@Override
		public void visit(Group grp) {
		}

		private void searchOpt(Context grp) {
			for (Option opt : grp.getOptions())
				if (opt.isValid(_cmdline.peek())) {
					opt.apply(receive, _cmdline.next(), _cmdline);
					throw new FoundOption(opt);
				}
		}
		
		public void visit(Command grp) {
			grp.getParent().accept(this);;
		}
		
		@Override
		public void visit(Context grp) {
			searchOpt(grp);

			Context p = grp.getParent();
			if (p != null) {
				receive = grp.getEnclosing(receive);
				p.accept(this);
			}
		}

		public boolean visit_options(Command cmd, Object group) {
			receive = group;
			try {
				if(cmd instanceof Group) {
					visit((Context)cmd);
				} else
					cmd.accept(this);
				return false;
			} catch (FoundOption e) {
				return true;
			}
		}

	}

	@SuppressWarnings("serial")
	static class FoundOption extends RuntimeException {
		Option opt;

		public FoundOption(Option opt) {
			this.opt = opt;
		}
	}
}
