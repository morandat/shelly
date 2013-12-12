package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Item;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.impl.ConverterFactory.BasicConverter;
import fr.labri.shelly.impl.Visitor.TraversalVisitor;
import static fr.labri.shelly.annotations.AnnotationUtils.*;

public interface ModelBuilder<C, M> {
	public Group<C, M> createModel(C clazz);

	public class Executable implements ModelBuilder<Class<?>, Member> {
		public Group<Class<?>, Member> createModel(Class<?> clazz) {
			fr.labri.shelly.annotations.Group annotation = clazz.getAnnotation(CMDGRP_CLASS);
			if (annotation == null)
				throw new RuntimeException("Cannot create model from a non command group class " + clazz);
			
			return new Builder().build(annotation, clazz);
		}
		
		public String accessorName(String str) {
			if(str.startsWith("set"))
				str = str.substring(3);
			return str.toLowerCase();
		}
		private class Builder extends TraversalVisitor<Class<?>, Member> {
			ConverterFactory _parentConverter = fr.labri.shelly.impl.ConverterFactory.DEFAULT;
			ExecutableModelFactory _parentFactory = fr.labri.shelly.impl.ExecutableModelFactory.EXECUTABLE_MODEL;

			public Group<Class<?>, Member> build(fr.labri.shelly.annotations.Group name, Class<?> clazz) {
				Group<Class<?>, Member> grp = createGroup(null, name, clazz);
				visit(grp);
				return grp;
			}

			@Override
			public void visit(Composite<Class<?>, Member> optionGroup) {
				ConverterFactory p = _parentConverter;
				populate((Composite<Class<?>, Member>) optionGroup, optionGroup.getAssociatedElement());
				super.visit(optionGroup);
				_parentConverter = p;
			}

			protected void populate(Composite<Class<?>, Member> grp, Class<?> clazz) {
				for (Field f : clazz.getFields())
					if (f.isAnnotationPresent(OPT_CLASS))
						grp.addOption(createItem(f.getAnnotation(OPT_CLASS), f, grp));
				for (Method m : clazz.getMethods())
					if (m.isAnnotationPresent(CMD_CLASS))
						grp.addCommand(createItem(m.getAnnotation(CMD_CLASS), m, grp));
					else if (m.isAnnotationPresent(OPT_CLASS))
							grp.addOption(createItem(m.getAnnotation(OPT_CLASS), m, grp));

				for (Class<?> c : clazz.getClasses())
					if (c.isAnnotationPresent(CMDGRP_CLASS))
						grp.addCommand(createGroup(c.getAnnotation(CMDGRP_CLASS), c, Modifier.isStatic(c.getModifiers()) ? null : grp));
					else if (c.isAnnotationPresent(OPTGRP_CLASS))
						grp.addCommand(createContext(c.getAnnotation(OPTGRP_CLASS), c, Modifier.isStatic(c.getModifiers()) ? null : grp));
			}

			public Composite<Class<?>, Member> createContext(Composite<Class<?>, Member> parent, fr.labri.shelly.annotations.Context annotation, Class<?> clazz) {
				if ((parent == null) != (clazz.getEnclosingClass() == null))
					throw new RuntimeException("Cannot create option group when not starting at top level");
				String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
				ExecutableModelFactory factory = ExecutableModelFactory.loadModelFactory(_parentFactory, AnnotationUtils.getFactory(annotation));

				Composite<Class<?>, Member> item = factory.newContext(name, parent, clazz);
				return item;
			}

			public Group<Class<?>, Member> createGroup(Composite<Class<?>, Member> parent, fr.labri.shelly.annotations.Group annotation, Class<?> clazz) {
				if ((parent == null) != (clazz.getEnclosingClass() == null))
					throw new RuntimeException("Cannot create option group when not starting at top level"); // FIXME
				String name = getName(annotation.name(), clazz.getSimpleName().toLowerCase());
				ExecutableModelFactory factory = ExecutableModelFactory.loadModelFactory(_parentFactory, AnnotationUtils.getFactory(annotation));
				
				return factory.newGroup(name, parent, clazz);
			}
			
			protected Option<Class<?>, Member> createItem(fr.labri.shelly.annotations.Option annotation, Field field, Composite<Class<?>, Member> parent) {
				String name = getName(annotation.name(), field.getName().toLowerCase());
				ConverterFactory converter = loadConverterFactory(annotation.converter());
				ModelFactory<Class<?>, Member> factory = ExecutableModelFactory.loadModelFactory(_parentFactory, AnnotationUtils.getFactory(annotation)); 
				return factory.newOption(converter, parent, name, field);
			}

			protected Option<Class<?>, Member> createItem(fr.labri.shelly.annotations.Option annotation, Method method, Composite<Class<?>, Member> parent) {
				String name = getName(annotation.name(), accessorName(method.getName()));
				ConverterFactory converter = loadConverterFactory(annotation.converter());
				ModelFactory<Class<?>, Member> factory = ExecutableModelFactory.loadModelFactory(_parentFactory, AnnotationUtils.getFactory(annotation)); 
				return factory.newOption(converter, parent, name, method);
			}

			protected Item<Class<?>, Member> createItem(fr.labri.shelly.annotations.Command annotation, Method method, Composite<Class<?>, Member> parent) {
				String name = getName(annotation.name(), method.getName().toLowerCase());
				ConverterFactory converter = loadConverterFactory(annotation.converter());
				ModelFactory<Class<?>, Member> factory = ExecutableModelFactory.loadModelFactory(_parentFactory, AnnotationUtils.getFactory(annotation)); 
				return factory.newCommand(converter, parent, name, method);
			}

			protected Item<Class<?>, Member> createGroup(fr.labri.shelly.annotations.Group annotation, Class<?> clazz, Composite<Class<?>, Member> parent) {
				return createGroup(parent, annotation, clazz);
			}

			protected Item<Class<?>, Member> createContext(fr.labri.shelly.annotations.Context annotation, Class<?> clazz, Composite<Class<?>, Member> parent) {
				return createContext(parent, annotation, clazz);
			}
			
			protected ConverterFactory loadConverterFactory(Class<? extends ConverterFactory> newFactory[]) {
				if (newFactory.length < 1 || BasicConverter.class.equals(newFactory[0]))
					return _parentConverter;
				return fr.labri.shelly.impl.ConverterFactory.getComposite(_parentConverter, newFactory);
			}
		}
	}

	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.Context> OPTGRP_CLASS = fr.labri.shelly.annotations.Context.class;
	static public final Class<fr.labri.shelly.annotations.Group> CMDGRP_CLASS = fr.labri.shelly.annotations.Group.class;
}
