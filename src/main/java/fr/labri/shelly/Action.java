package fr.labri.shelly;

public interface Action<C, M> extends Triggerable<C, M> {
	boolean isDefault();

	Object createContext(Object parent);
}
