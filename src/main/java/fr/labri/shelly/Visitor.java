package fr.labri.shelly;

public interface Visitor {

	void visit(ShellyItem item);
	void visit(OptionGroup optionGroup);
	void visit(Option option);
	void visit(Command cmd);
	void visit(CommandGroup cmdGroup);

}
