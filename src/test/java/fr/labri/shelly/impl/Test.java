package fr.labri.shelly.impl;

import demo.SimpleProject;
import fr.labri.shelly.Command;
import fr.labri.shelly.Group;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.impl.Visitor.CommandVisitor;

public class Test {
	public static void main(String[] args) {
		ModelFactory f = new ModelFactory();
		Group grp = f.createModel(SimpleProject.class);
		Shell.printHelp(SimpleProject.class);
		
		new PrintVisitor().print(grp);
	}
	
	static class PrintVisitor extends CommandVisitor {
		void print(Group grp) {
			grp.visit_commands(this);
			
		}
		public void visit(Context g) {
			super.visit(g);
		}
		public void visit(Group c) {
			System.out.println("****** "+c.getID() +" ******");
			HelpHelper.printHelp(c);
			print(c);
		}		
		public void visit(Command c) {
			System.out.println("****** "+c.getID() +" ******");
			HelpHelper.printHelp(c);
			super.visit(c);
		}
	}
}
