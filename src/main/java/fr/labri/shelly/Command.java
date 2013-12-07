package fr.labri.shelly;

public interface Command extends ShellyDescriptable {
	Object createContext(Object parent);
}
