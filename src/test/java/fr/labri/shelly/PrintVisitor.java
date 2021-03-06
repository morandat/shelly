package fr.labri.shelly;

import fr.labri.shelly.Visitor.VisitorAdapter;

public class PrintVisitor<C, M> extends VisitorAdapter<C, M> {
	public void print(Group<C, M> grp) {
		visit_actions(grp);
	}

	public void visit(Composite<C, M> g) {
		super.visit(g);
	}

	public void visit(Group<C, M> c) {
		System.out.println("****** " + c.getID() + " ******");
		HelpFactory.printHelp(c, System.out);
		print(c);
	}

	public void visit(Command<C, M> c) {
		System.out.println("****** " + c.getID() + " ******");
		HelpFactory.printHelp(c, System.out);
		super.visit(c);
	}
}
