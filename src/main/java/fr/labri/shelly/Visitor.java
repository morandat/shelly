package fr.labri.shelly;

public interface Visitor<C, M> {

	void visit(Item<C, M> item);
	void visit(Composite<C, M> optionGroup);
	void visit(Option<C, M> option);
	void visit(Command<C, M> cmd);
	void visit(Group<C, M> cmdGroup);
	void visit(Context<C, M> optionGroup);
	void visit(Terminal<C, M> option);
	void visit(Action<C, M> option);

}
