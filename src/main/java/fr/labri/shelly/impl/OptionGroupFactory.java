package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fr.labri.shelly.Command;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Option;

public class OptionGroupFactory {
	public static final OptionGroupFactory DEFAULT = new OptionGroupFactory();
	
	public ConverterFactory factory = new fr.labri.shelly.impl.ConverterFactory();
	
	public OptionGroup createGroup(Class<?> clazz) {
		OptionGroup grp = new OptionGroup(clazz);
		build(grp, clazz);
		return grp;
	}
	
	protected Option newItem(fr.labri.shelly.annotations.Option annotation, Field field) {
		String name = annotation.name() == null ? annotation.name() : field.getName();
		return new SimpleOption(loadFactory(annotation.factory()), name, field);
	}
	
	protected Command newItem(fr.labri.shelly.annotations.Command annotation, Method method) {
		String name = annotation.name() == null ? annotation.name() : method.getName();
		return new SimpleCommand(loadFactory(annotation.factory()), name, method);
	}
	
	protected Command newItem(fr.labri.shelly.annotations.CommandGroup annotation, Class<?> clazz) {
		return null;
	}
	
	protected Command newItem(fr.labri.shelly.annotations.OptionGroup annotation, Class<?> clazz) {
		//return createGroup();
		return null;
	}
	
	private ConverterFactory loadFactory(Class<? extends ConverterFactory> newFactory) {
		if(factory.getClass().equals(newFactory))
			return factory;
		try {
			return newFactory.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void build(OptionGroup grp, Class<?> clazz) {
		for (Field f : clazz.getFields())
			if (f.isAnnotationPresent(OPT_CLASS)) 
				grp.addOption(newItem(f.getAnnotation(OPT_CLASS), f));
		for (Method m : clazz.getMethods())
			if (m.isAnnotationPresent(CMD_CLASS))
				grp.addCommand(newItem(m.getAnnotation(CMD_CLASS), m));
		for (Class<?> c : clazz.getClasses())
			if (c.isAnnotationPresent(CMDGRP_CLASS))
				grp.addCommand(newItem(c.getAnnotation(CMDGRP_CLASS), c));
			else if (c.isAnnotationPresent(OPTGRP_CLASS))
				grp.addCommand(newItem(c.getAnnotation(OPTGRP_CLASS), c));
	}
	
	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.OptionGroup> OPTGRP_CLASS = fr.labri.shelly.annotations.OptionGroup.class;
	static public final Class<fr.labri.shelly.annotations.CommandGroup> CMDGRP_CLASS = fr.labri.shelly.annotations.CommandGroup.class;
}
