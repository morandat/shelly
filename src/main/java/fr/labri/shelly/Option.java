package fr.labri.shelly;


public interface Option<C, M> extends Terminal<C, M> {
	public boolean isValidShortOption(Recognizer parser, char str);
	String getFlags();
}
