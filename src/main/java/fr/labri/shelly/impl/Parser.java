package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.impl.Visitor.CommandVisitor;

public class Parser {
	PeekIterator<String> _cmdline;

	public void execute(Group cmd, final PeekIterator<String> cmdline) {
		_cmdline = cmdline;
		Object ctx = fillOptions(cmd, cmd.newGroup(null));
		Command subCmd = cmd;
		while((subCmd = new CommandParserVisitor().find_command(subCmd)) != null)
			ctx = executeGroup(subCmd, ctx);
	}

	private Object executeGroup(Command subCmd, Object parent) {
		String txt = _cmdline.next();
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

	@SuppressWarnings("serial")
	static class FoundCommand extends RuntimeException {
		Command cmd;

		public FoundCommand(Command cmd) {
			this.cmd = cmd;
		}
	}

	class CommandParserVisitor extends CommandVisitor {
		@Override
		public void visit(Command cmd) {
			if (cmd.isValid(_cmdline.peek())) {
				throw new FoundCommand(cmd);
			}
		}

		public Command find_command(Command grp) {
			try {
				if(grp instanceof Group)
					((Group)grp).visit_commands(this);
			} catch (FoundCommand e) {
				System.out.println("found cmd: "+e.cmd.getID() + " from " +grp.getID());
				return e.cmd;
			}
			return null;
		}
	}

}
