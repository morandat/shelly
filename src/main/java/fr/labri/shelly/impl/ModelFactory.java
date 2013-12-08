package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import static fr.labri.shelly.annotations.AnnotationUtils.*;
import fr.labri.shelly.impl.Visitor.TraversalVisitor;

public class ModelFactory {
	public static final ModelFactory DEFAULT = new ModelFactory();

	public ConverterFactory factory = new fr.labri.shelly.impl.ConverterFactory();

	private ConverterFactory loadFactory(Class<? extends ConverterFactory> newFactory) {
		if (factory.getClass().equals(newFactory))
			return factory;
		try {
			return newFactory.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public Group createModel(Class<?> clazz) {
		fr.labri.shelly.annotations.Group annotation = clazz.getAnnotation(CMDGRP_CLASS);
		if (annotation == null)
			throw new RuntimeException("Cannot create model from a non command group class " + clazz);
		String name = annotation.name() == fr.labri.shelly.annotations.Option.NO_NAME ? annotation.name() : clazz.getSimpleName().toLowerCase();

		return new Builder().build(name, clazz);
	}

	static public String accessorName(String str) {
		if(str.startsWith("set"))
			str = str.substring(3);
		return str.toLowerCase();
	}

	private class Builder extends TraversalVisitor {
		public Builder() {
		}

		public Group build(String name, Class<?> clazz) {
			Group grp = createCommandGroup(null, name, clazz);
			visit(grp);
			return grp;
		}

		@Override
		public void visit(Context optionGroup) {
			populate((Context) optionGroup, optionGroup.getAssociatedClass());
			super.visit(optionGroup);
		}

		public Context createGroup(Context parent, String name, Class<?> clazz) {
			if ((parent == null) != (clazz.getEnclosingClass() == null))
				throw new RuntimeException("Cannot create option group when not starting at top level"); // FIXME
			return AbstractContext.getContext(name, parent, clazz, null);
		}

		public Group createCommandGroup(Context parent, String name, Class<?> clazz) {
			if ((parent == null) != (clazz.getEnclosingClass() == null))
				throw new RuntimeException("Cannot create option group when not starting at top level"); // FIXME
			return AbstractGroup.getGroup(name, parent, clazz, null);
		}

		protected void populate(Context grp, Class<?> clazz) {

			for (Field f : clazz.getFields())
				if (f.isAnnotationPresent(OPT_CLASS))
					grp.addOption(newItem(f.getAnnotation(OPT_CLASS), f, grp));
			for (Method m : clazz.getMethods())
				if (m.isAnnotationPresent(CMD_CLASS))
					grp.addCommand(newItem(m.getAnnotation(CMD_CLASS), m, grp));
				else if (m.isAnnotationPresent(OPT_CLASS))
						grp.addOption(newItem(m.getAnnotation(OPT_CLASS), m, grp));

			for (Class<?> c : clazz.getClasses())
				if (c.isAnnotationPresent(CMDGRP_CLASS))
					grp.addCommand(newItem(c.getAnnotation(CMDGRP_CLASS), c, Modifier.isStatic(c.getModifiers()) ? null : grp));
				else if (c.isAnnotationPresent(OPTGRP_CLASS))
					grp.addCommand(newItem(c.getAnnotation(OPTGRP_CLASS), c, Modifier.isStatic(c.getModifiers()) ? null : grp));
		}

		protected Option newItem(fr.labri.shelly.annotations.Option annotation, Field field, fr.labri.shelly.Context parent) {
			String name = getName(annotation.name(), field.getName().toLowerCase());
			return OptionFactory.build(loadFactory(annotation.factory()), parent, name, field);
		}

		protected Option newItem(fr.labri.shelly.annotations.Option annotation, Method method, fr.labri.shelly.Context parent) {
			String name = getName(annotation.name(), accessorName(method.getName()));
			return OptionFactory.build(loadFactory(annotation.factory()), parent, name, method);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Command annotation, Method method, Context parent) {
			String name = getName(annotation.name(), method.getName().toLowerCase());
			return CommandFactory.build(loadFactory(annotation.factory()), parent, name, method);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Group annotation, Class<?> clazz, Context parent) {
			String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
			return createCommandGroup(parent, name, clazz);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Context annotation, Class<?> clazz, Context parent) {
			String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
			return createGroup(parent, name, clazz);
		}
	}

	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.Context> OPTGRP_CLASS = fr.labri.shelly.annotations.Context.class;
	static public final Class<fr.labri.shelly.annotations.Group> CMDGRP_CLASS = fr.labri.shelly.annotations.Group.class;
}
