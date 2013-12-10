package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.impl.ConverterFactory.BasicConverter;
import fr.labri.shelly.impl.Visitor.TraversalVisitor;
import static fr.labri.shelly.annotations.AnnotationUtils.*;

public class ModelBuilder {
	public Group createModel(Class<?> clazz) {
		fr.labri.shelly.annotations.Group annotation = clazz.getAnnotation(CMDGRP_CLASS);
		if (annotation == null)
			throw new RuntimeException("Cannot create model from a non command group class " + clazz);

		return new Builder().build(annotation, clazz);
	}

	static public String accessorName(String str) {
		if(str.startsWith("set"))
			str = str.substring(3);
		return str.toLowerCase();
	}

	private class Builder extends TraversalVisitor {
		ConverterFactory _parentConverter = fr.labri.shelly.impl.ConverterFactory.DEFAULT;
		ModelFactory _parentFactory = fr.labri.shelly.impl.ModelFactory.EXECUTABLE_MODEL;

		public Group build(fr.labri.shelly.annotations.Group name, Class<?> clazz) {
			Group grp = createGroup(null, name, clazz);
			visit(grp);
			return grp;
		}

		@Override
		public void visit(Context optionGroup) {
			ConverterFactory p = _parentConverter;
			populate((Context) optionGroup, optionGroup.getAssociatedClass());
			super.visit(optionGroup);
			_parentConverter = p;
		}

		public Context createContext(Context parent, fr.labri.shelly.annotations.Context annotation, Class<?> clazz) {
			if ((parent == null) != (clazz.getEnclosingClass() == null))
				throw new RuntimeException("Cannot create option group when not starting at top level");
			String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
			ModelFactory factory = loadModelFactory(AnnotationUtils.getFactory(annotation));

			return factory.newContext(name, parent, clazz, null);
		}

		public Group createGroup(Context parent, fr.labri.shelly.annotations.Group annotation, Class<?> clazz) {
			if ((parent == null) != (clazz.getEnclosingClass() == null))
				throw new RuntimeException("Cannot create option group when not starting at top level"); // FIXME
			String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
			ModelFactory factory = loadModelFactory(AnnotationUtils.getFactory(annotation));
			
			return factory.newGroup(name, parent, clazz, null);
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
			ConverterFactory converter = loadConverterFactory(annotation.converter());
			ModelFactory factory = loadModelFactory(AnnotationUtils.getFactory(annotation)); 
			return factory.newOption(converter, parent, name, field);
		}

		protected Option newItem(fr.labri.shelly.annotations.Option annotation, Method method, fr.labri.shelly.Context parent) {
			String name = getName(annotation.name(), accessorName(method.getName()));
			ConverterFactory converter = loadConverterFactory(annotation.converter());
			ModelFactory factory = loadModelFactory(AnnotationUtils.getFactory(annotation)); 
			return factory.newOption(converter, parent, name, method);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Command annotation, Method method, Context parent) {
			String name = getName(annotation.name(), method.getName().toLowerCase());
			ConverterFactory converter = loadConverterFactory(annotation.converter());
			ModelFactory factory = loadModelFactory(AnnotationUtils.getFactory(annotation)); 
			return factory.newCommand(converter, parent, name, method);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Group annotation, Class<?> clazz, Context parent) {
			return createGroup(parent, annotation, clazz);
		}

		protected ShellyItem newItem(fr.labri.shelly.annotations.Context annotation, Class<?> clazz, Context parent) {
			return createContext(parent, annotation, clazz);
		}
		
		protected ConverterFactory loadConverterFactory(Class<? extends ConverterFactory> newFactory[]) {
			if (newFactory.length < 1 || BasicConverter.class.equals(newFactory[0]))
				return _parentConverter;
			return fr.labri.shelly.impl.ConverterFactory.getComposite(_parentConverter, newFactory);
		}
		
		protected ModelFactory loadModelFactory(Class<? extends ModelFactory> newFactory) {
			ModelFactory factory = _parentFactory;
			if(newFactory != null)
				factory = factory.instantiateFactory(newFactory);
			return factory;
		}
	}

	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.Context> OPTGRP_CLASS = fr.labri.shelly.annotations.Context.class;
	static public final Class<fr.labri.shelly.annotations.Group> CMDGRP_CLASS = fr.labri.shelly.annotations.Group.class;
}