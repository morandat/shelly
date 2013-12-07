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
	
	static class TraversalVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group cmdGroup) {
			visit((Context)cmdGroup);
		}
	}
	
	static class OptionVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			visit_parent(item);
		}
		
		@Override
		public void visit(Context grp) {
			grp.visit_options(this);
			visit_parent(grp);
		}
		
		@Override
		public void visit(Group grp) {
		}
	}
	
	static class CommandVisitor extends Visitor {
		
		@Override
		public void visit(Group cmdGrp) {
			visit((Command) cmdGrp);
		}
		
		@Override
		public void visit(Context cmd) {
			cmd.visit_commands(this);
		}
	}
}
