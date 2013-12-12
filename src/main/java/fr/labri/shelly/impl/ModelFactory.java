package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;

public interface ModelFactory<C, M> {
	
	public abstract Composite<C, M> newContext(String name, Composite<C, M> parent, C clazz);
	public abstract Group<C, M> newGroup(String name, Composite<C, M> parent, C clazz); 
	public abstract Command<C, M> newCommand(ConverterFactory loadFactory, Composite<C, M> parent, String name, final M member);
	public abstract Option<C, M> newOption(ConverterFactory loadFactory, Composite<C, M> parent, String name, final M member);
}
