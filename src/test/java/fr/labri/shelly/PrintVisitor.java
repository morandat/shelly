package fr.labri.shelly;

import fr.labri.shelly.impl.Context;
import fr.labri.shelly.impl.HelpHelper;
import fr.labri.shelly.impl.Visitor;

public class PrintVisitor extends Visitor {
	public void print(Group grp) {
		grp.visit_commands(this);

	}

	public void visit(Context g) {
		super.visit(g);
	}

	public void visit(Group c) {
		System.out.println("****** " + c.getID() + " ******");
		HelpHelper.printHelp(c);
		print(c);
	}

	public void visit(Command c) {
		System.out.println("****** " + c.getID() + " ******");
		HelpHelper.printHelp(c);
		super.visit(c);
	}
}
