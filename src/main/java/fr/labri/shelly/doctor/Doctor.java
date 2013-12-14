package fr.labri.shelly.doctor;

import java.lang.reflect.Member;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor7;

import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Context;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.Item;
import fr.labri.shelly.ModelFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Terminal;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.AnnotationUtils.ElementValue;
import fr.labri.shelly.impl.AbstractGroup;
import fr.labri.shelly.impl.AbstractContext;
import fr.labri.shelly.impl.AbstractCommand;
import fr.labri.shelly.impl.AbstractOption;
import fr.labri.shelly.impl.DescriptionFactory;
import fr.labri.shelly.impl.ExecutableModelFactory;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.ModelBuilder;
import fr.labri.shelly.impl.Visitor;
import static fr.labri.shelly.ModelFactory.*;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value = { "fr.labri.shelly.annotations.*" })
public class Doctor extends AbstractProcessor {
	ElementModelBuilder ELEMENT_MODEL_BUILDER = new ElementModelBuilder();
	ElementModelFactory ELEMENT_MODEL_FACTORY = new ElementModelFactory();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Set: " + annotations + "\nEnv: " + roundEnv);
		for (Element elt : roundEnv.getRootElements()) {
			// if (elt.getAnnotation(GROUP_CLASS) == null) // FIXME, do we have to look further before giving the warning ?
			// warn("Top level group %s class has no @Group annotation, let skip it.", elt);
			// // else // i.e., is this else mandatory.
			// Composite<TypeElement, Element> grp = check(elt);
			createModel(elt);
		}
		return true;
	}

	public Group<TypeElement, Element> createModel(Element elt) {
		if (!(elt instanceof TypeElement))
			error("Root element is not a class: %s", elt);
		Group<TypeElement, Element> grp = ELEMENT_MODEL_BUILDER.createModel((TypeElement) elt);
		System.err.println(grp);

		grp.startVisit(new Visitor.TraversalVisitor<TypeElement, Element>() {
			public void visit(Context<TypeElement, Element> i) {
				super.visit(i);
			}

			public void visit(Group<TypeElement, Element> i) {
				HelpFactory.printHelp(i, System.out);
				super.visit(i);
			}

			public void visit(Terminal<TypeElement, Element> i) {
				HelpFactory.printHelp(i, System.out);
			}
		});

		HelpFactory.printHelp(grp, System.out);
		return grp;
	}

	protected void warn(String msg, Object... args) {
		// Messager.
		// printMessage(Kind.ERROR, "method wasn't public and final", element)
		String text = String.format("[Shelly] %s\n", String.format(msg, args));
		System.err.println(text);
	}

	protected void error(String msg, Object... args) {
		warn(msg, args);
		throw new RuntimeException("error");
	}

	// private Composite<TypeElement, Element> check(Element elt) {
	// return elt.accept(new StrutureVisitor(), null);
	// }

	class ElementModelBuilder extends ModelBuilder<TypeElement, Element> {
		@Override
		public Group<TypeElement, Element> createModel(TypeElement clazz) {
			return createModel(clazz.getAnnotation(GROUP_CLASS), clazz);
		}

		@Override
		public ElementBuilder newBuilder() {
			return new ElementBuilder();
		}

		class ElementBuilder extends Builder<TypeElement, Element> {
			@Override
			protected ModelFactory<TypeElement, Element> getFactory(Class<? extends ExecutableModelFactory> parent) {
				return ELEMENT_MODEL_FACTORY;
			}

			@Override
			protected ConverterFactory getConverterFactory(Class<? extends ConverterFactory>[] classes) {
				return null;
			}

			@Override
			protected String getCName(TypeElement clazz) {
				return clazz.getSimpleName().toString();
			}

			@Override
			protected String getMName(Element member) {
				return member.getSimpleName().toString();
			}

			@Override
			protected void populate(Composite<TypeElement, Element> optionGroup) {
				for (Element elt : optionGroup.getAssociatedElement().getEnclosedElements())
					elt.accept(new CreateItem(), optionGroup);
			}

			class CreateItem extends ElementKindVisitor7<Void, Composite<TypeElement, Element>> {
				@Override
				public Void visitTypeAsClass(TypeElement e, Composite<TypeElement, Element> p) {
					if (e.getAnnotation(GROUP_CLASS) != null)
						p.addItem(createGroup(p, e.getAnnotation(GROUP_CLASS), e));
					else if (e.getAnnotation(CONTEXT_CLASS) != null)
						p.addItem(createContext(p, e.getAnnotation(CONTEXT_CLASS), e));
					else
						checkShellyAnotations(e);
					return defaultAction(e, p);
				}

				@Override
				public Void visitVariableAsField(VariableElement e, Composite<TypeElement, Element> p) {
					if (e.getAnnotation(OPT_CLASS) != null)
						p.addItem(createOption(e.getAnnotation(OPT_CLASS), e, p));
					return defaultAction(e, p);
				}

				@Override
				public Void visitExecutableAsMethod(ExecutableElement e, Composite<TypeElement, Element> p) {
					if (e.getAnnotation(CMD_CLASS) != null)
						p.addItem(createCommand(e.getAnnotation(CMD_CLASS), e, p));
					else if (e.getAnnotation(OPT_CLASS) != null)
						p.addItem(createOption(e.getAnnotation(OPT_CLASS), e, p));
					return defaultAction(e, p);
				}
			}
		}
	}

	class StrutureVisitor extends ElementKindVisitor7<Composite<TypeElement, Element>, Composite<TypeElement, Element>> {
		@Override
		public Composite<TypeElement, Element> visitTypeAsClass(TypeElement e, Composite<TypeElement, Element> parent) {
			Composite<TypeElement, Element> g;
			if (e.getAnnotation(CONTEXT_CLASS) != null) {
				g = ELEMENT_MODEL_FACTORY.newContext(e.getSimpleName().toString(), parent, e);
				checkContext((Context<TypeElement, Element>) g);
			} else if (e.getAnnotation(GROUP_CLASS) != null) {
				g = ELEMENT_MODEL_FACTORY.newGroup(e.getSimpleName().toString(), parent, e);
				checkGroup((Group<TypeElement, Element>) g);
			} else {
				g = ELEMENT_MODEL_FACTORY.newGroup(e.getSimpleName().toString(), parent, e);
				checkShellyAnotations(e);
			}

			// model_factory
			return g;
		}

		//
		// @Override
		// public Group visitVariableAsField(VariableElement e, Void p) {
		// System.out.println(e);
		// return super.visitVariableAsField(e, p);
		// }
		//
		// @Override
		// public Group visitExecutableAsMethod(ExecutableElement e, Void p) {
		// System.out.println(e);
		// return super.visitExecutableAsMethod(e, p);
		// }

		public void checkContext(Context<TypeElement, Element> context) {
			System.out.println("Check context ");
			System.out.println("Default " + context);
		}

		public void checkGroup(Group<TypeElement, Element> grp) {
			System.out.println("Check group " + grp.getID());
			System.out.println("Default " + grp);
		}
	}

	boolean containsOne(Element e) {
		for (AnnotationMirror c : e.getAnnotationMirrors())
			if (ModelFactory.SHELLY_ANNOTATIONS.contains(c.getClass()))
				return true;
		return false;
	}

	public void checkShellyAnotations(TypeElement e) {
		if (e.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT))
			return; // We do not check abstract classes
		for (Element elt : e.getEnclosedElements()) {
			if (containsOne(e))
				warn("%s has Shelly annotation but %s has no Shelly annotation", elt, e);
			if (elt instanceof TypeElement)
				checkShellyAnotations((TypeElement) elt);
		}
	}

	class ElementModelFactory implements ModelFactory<TypeElement, Element> {
		public Group<TypeElement, Element> newGroup(final String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			final boolean isDefault = clazz.getAnnotation(DEFAULT_CLASS) != null;
			return new AbstractGroup<TypeElement, Element>(parent, name, clazz) {
				@Override
				public Description getDescription() {
					return DescriptionFactory.getGroupDescription(this, _clazz, SUMMARY.getGroup(_clazz));
				}

				@Override
				public boolean isDefault() {
					return isDefault;
				}
			};
		}

		@Override
		public Context<TypeElement, Element> newContext(String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			return new AbstractContext<TypeElement, Element>(parent, name, clazz) {
			};
		}

		@Override
		public Command<TypeElement, Element> newCommand(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, final String name,
				final Element member) {
			final boolean isDefault = member.getAnnotation(DEFAULT_CLASS) != null;

			return new AbstractCommand<TypeElement, Element>(name, parent, member) {
				@Override
				public boolean isDefault() {
					return isDefault;
				}

				@Override
				public Description getDescription() {
					return DescriptionFactory.getDescription(member, name);
				}
			};
		}

		@Override
		public Option<TypeElement, Element> newOption(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, final String name,
				final Element member) {
			return new AbstractOption<TypeElement, Element>(parent, name, member) {
				@Override
				public Description getDescription() {
					return DescriptionFactory.getDescription(member, name);
				}
			};
		}
	};
	
	public final static ElementValue<String> SUMMARY = new ElementValue<String>("summary", fr.labri.shelly.annotations.Option.NO_NAME);
}
