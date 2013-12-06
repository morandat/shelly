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
	}
	
	static class OptionVisitor extends Visitor {
		@Override
		public void visit(ShellyItem item) {
			ShellyItem parent = item.getParent();
			if(parent != null)
				parent.accept(this);
		}
	}
	
	static class CommandVisitor extends TraversalVisitor {
		@Override
		public void visit(ShellyItem item) {
		}
	}
}
