package fr.labri.shelly.impl;

import java.lang.reflect.Member;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;

public abstract class ModelFactory<C, M> {
	
	public abstract Context<C, M> newContext(String name, Context<C, M> parent, C clazz);
	public abstract Group<C, M> newGroup(String name, Context<C, M> parent, C clazz); 
	public abstract Command<C, M> newCommand(ConverterFactory loadFactory, Context<C, M> parent, String name, final Member member);
	public abstract Option<C, M> newOption(ConverterFactory loadFactory, Context<C, M> parent, String name, final Member member);
}
