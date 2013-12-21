package fr.labri.shelly;

public interface Triggerable<C, M> extends Item<C, M> {
	public int isValid(Recognizer parser, String str, int index);

	void execute(Object receive, String string, Executor executor);
	Description getDescription();
}
