package fr.labri.shelly;

public interface Visitor<C, M> {

	void visit(ShellyItem<C, M> item);
	void visit(Context<C, M> optionGroup);
	void visit(Option<C, M> option);
	void visit(Command<C, M> cmd);
	void visit(Group<C, M> cmdGroup);

}
