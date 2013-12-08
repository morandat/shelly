package fr.labri.shelly;

import fr.labri.shelly.impl.HelpFactory;
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
		HelpFactory.printHelp(c, System.out);
		print(c);
	}

	public void visit(Command c) {
		System.out.println("****** " + c.getID() + " ******");
		HelpFactory.printHelp(c, System.out);
		super.visit(c);
	}
}
