package fr.labri.shelly;

public interface Visitor {

	void visit(ShellyItem item);
	void visit(Context optionGroup);
	void visit(Option option);
	void visit(Command cmd);
	void visit(Group cmdGroup);

}
