package fr.labri.shelly;

public interface Command<C, M> extends ShellyDescriptable<C, M> {
	Object createContext(Object parent);
	boolean isDefault();
}
