package fr.labri.shelly;

public interface Action<C, M> extends Triggerable<C, M> {
	Object createContext(Object parent);
}
