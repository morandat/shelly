package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Context;
import fr.labri.shelly.ShellyItem;

public class Visitor implements fr.labri.shelly.Visitor {
	@Override
	public void visit(ShellyItem item) {
	}
	
	public void visit_parent(ShellyItem item) {
		ShellyItem p = item.getParent();
		if(p != null)
			p.accept(this);
	}
	
	@Override
	public void visit(Option option) {
		visit((ShellyItem)option);
	}
	
	@Override
	public void visit(Command cmd) {
		visit((ShellyItem)cmd);
	}

	@Override
	public void visit(Context optionGroup) {
		visit((ShellyItem)optionGroup);
	}

	@Override
	public void visit(Group cmdGroup) {
		visit((ShellyItem)cmdGroup);
	}
	
	public static class TraversalVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group cmdGroup) {
			visit((Context)cmdGroup);
		}
	}
	
	public static class OptionVisitor extends Visitor {
		public void visit(Group grp) {
		}

		public void visit(Command grp) {
			visit_parent(grp);
		}

		@Override
		public void visit(Context grp) {
			grp.visit_options(this);
			visit_parent(grp);
		}

		public void visit_options(Command cmd) {
			if (cmd instanceof Group) {
				visit((Context) cmd);
			} else
				cmd.accept(this);
		}
	}

	
	public static class CommandVisitor extends Visitor {
		
		@Override
		public void visit(Group cmdGrp) {
			visit((Command) cmdGrp);
		}
		
		@Override
		public void visit(Context cmd) {
			cmd.visit_commands(this);
		}
		
		public void visit_commands(Command cmd) {
			if (cmd instanceof Group) {
				visit((Context) cmd);
			} else
				cmd.accept(this);
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundCommand extends RuntimeException {
		public Command cmd;

		public FoundCommand(Command cmd) {
			this.cmd = cmd;
		}
	}
	

	@SuppressWarnings("serial")
	public static class FoundOption extends RuntimeException {
		public Option opt;

		public FoundOption(Option opt) {
			this.opt = opt;
		}
	}
	static class InstVisitor extends Visitor {
		private Object group;
		@Override
		public void visit(Group cmdGroup) {
		}
		@Override
		public void visit(Context ctx) {
			visit_parent(ctx);
			group = ctx.newGroup(group);
		}
		@Override
		public void visit(Command cmdGroup) {
			visit_parent(cmdGroup);
		}
		public Object instantiate(Command cmd, Object lastValidParent) {
			group = lastValidParent;
			if(cmd instanceof Group)
				visit((Context) cmd);
			else
				cmd.accept(this);
			return group;
		}
	}
}
