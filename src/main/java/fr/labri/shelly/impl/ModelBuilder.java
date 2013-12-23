package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.ModelFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor.TraversalVisitor;
import static fr.labri.shelly.impl.AnnotationUtils.*;

public abstract class ModelBuilder<C, M> {
	abstract public Group<C, M> createModel(C clazz);

	abstract public Builder<C, M> newBuilder();

	public Group<C, M> createModel(fr.labri.shelly.annotations.Group annotation, C clazz) {
		return createModel(annotation, clazz, true);
	}

	public Group<C, M> createModel(fr.labri.shelly.annotations.Group annotation, C clazz, boolean strict) {
		if (annotation == null)
			throw new RuntimeException("Cannot create model from a non command group class " + clazz);

		return newBuilder().build(annotation, clazz);
	}

	public String accessorName(String str) {
		if (str.startsWith("set"))
			str = str.substring(3);
		return str.toLowerCase();
	}

	public static abstract class Builder<C, M> extends TraversalVisitor<C, M> {
		@Override
		public void visit(Composite<C, M> optionGroup) {
			populate(optionGroup);
			super.visit(optionGroup);
		}

		protected Group<C, M> build(fr.labri.shelly.annotations.Group name, C clazz) {
			Group<C, M> grp = createGroup(null, name, clazz);
			visit(grp);
			return grp;
		}

		protected Group<C, M> createGroup(Composite<C, M> parent, fr.labri.shelly.annotations.Group annotation, C clazz) {
			String name = getName(annotation.name(), getCName(clazz).toLowerCase());
			return getFactory(AnnotationUtils.getFactory(annotation)).newGroup(name, parent, clazz);
		}

		protected Context<C, M> createContext(Composite<C, M> parent, fr.labri.shelly.annotations.Context annotation, C clazz) {
			String name = getName(annotation.name(), getCName(clazz).toLowerCase());
			return getFactory(AnnotationUtils.getFactory(annotation)).newContext(name, parent, clazz);
		}

		protected Option<C, M> createOption(fr.labri.shelly.annotations.Option annotation, M member, Composite<C, M> parent) {
			String name = getName(annotation.name(), getMName(member).toLowerCase());
			return getFactory(AnnotationUtils.getFactory(annotation)).newOption(getConverterFactory(AnnotationUtils.getConverterFactory(annotation)), parent,
					name, member);
		}

		protected Command<C, M> createCommand(fr.labri.shelly.annotations.Command annotation, M member, Composite<C, M> parent) {
			String name = getName(annotation.name(), getMName(member).toLowerCase());
			return getFactory(AnnotationUtils.getFactory(annotation)).newCommand(getConverterFactory(AnnotationUtils.getConverterFactory(annotation)), parent,
					name, member);
		}

		abstract protected ModelFactory<C, M> getFactory(Class<? extends ExecutableModelFactory> parent);

		abstract protected ConverterFactory getConverterFactory(Class<? extends ConverterFactory>[] classes);

		abstract protected String getCName(C clazz);

		abstract protected String getMName(M member);

		abstract protected void populate(Composite<C, M> optionGroup);
	}
}
