package fr.labri.shelly.impl;

import demo.SimpleProject;
import fr.labri.shelly.Command;
import fr.labri.shelly.CommandGroup;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.impl.Visitor.CommandVisitor;

public class Test {
	public static void main(String[] args) {
		ModelFactory f = new ModelFactory();
		CommandGroup grp = f.createModel(SimpleProject.class);
		Shell.printHelp(SimpleProject.class);
		
		new PrintVisitor().print(grp);
	}
	
	static class PrintVisitor extends CommandVisitor {
		void print(CommandGroup grp) {
			grp.visit_commands(this);
		}
		public void visit(Command c) {
			HelpHelper.printHelp(c);
		}
		public void visit(CommandGroup c) {
			HelpHelper.printHelp(c);
		}
	}
}
