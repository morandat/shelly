package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.CommandGroup;
import fr.labri.shelly.Option;
import fr.labri.shelly.OptionGroup;
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
	public void visit(OptionGroup optionGroup) {
		visit((ShellyItem)optionGroup);
	}

	@Override
	public void visit(CommandGroup cmdGroup) {
		visit((ShellyItem)cmdGroup);
	}
	
	static class TraversalVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			item.visit_all(this);
		}
		@Override
		public void visit(CommandGroup cmdGroup) {
			visit((OptionGroup)cmdGroup);
		}
	}
	
	static class OptionVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			visit_parent(item);
		}
		
		@Override
		public void visit(OptionGroup grp) {
			grp.visit_options(this);
			visit((ShellyItem)grp);
		}
		
		@Override
		public void visit(CommandGroup grp) {
		}
	}
	
	static class CommandVisitor extends Visitor {
		
		@Override
		public void visit(CommandGroup cmdGrp) {
			visit((Command) cmdGrp);
		}
		
		@Override
		public void visit(fr.labri.shelly.OptionGroup cmd) {
			cmd.visit_commands(this);
		}
	}
}
