package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor7;

import fr.labri.shelly.Action;
import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Param;


@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value= {"fr.labri.shelly.annotations.*"})
public class Doctor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Set: "+ annotations + "\nEnv: "+roundEnv);
		for (Element elt : roundEnv.getRootElements()) {
			if(elt.getAnnotation(GROUP_CLASS) == null) // FIXME, do we have to look further before giving the warning ?
				warn("Top level group %s class has no @Group annotation, let skip it.", elt);
//			else // i.e., is this else mandatory.
			Group<TypeElement, Element> grp = check(elt);
		}
		return true;
	}
	
	private Group<TypeElement, Element> check(Element elt) {
		return elt.accept(new StrutureVisitor(), null);
	}
	
	public void warn(String msg, Object... args) {
		String text = String.format("[Shelly] %s\n", String.format(msg, args));
		System.err.println(text);
	}
	
	class StrutureVisitor extends ElementKindVisitor7<Group<TypeElement, Element>, Composite<TypeElement, Element>> {
		@Override
		public Group<TypeElement, Element> visitTypeAsClass(TypeElement e, Composite<TypeElement, Element> parent) {
			if(e.getAnnotation(CONTEXT_CLASS) != null)
				checkContext(e);
			else if(e.getAnnotation(GROUP_CLASS) != null)
				checkGroup(e);
			else {
				checkShellyAnotations(e);
				return null;
			}
			
			Group<TypeElement, Element> grp = model_factory.newGroup(e.getSimpleName().toString(), parent, e);
//			model_factory
			return grp;
		}
//
//		@Override
//		public Group visitVariableAsField(VariableElement e, Void p) {
//			System.out.println(e);
//			return super.visitVariableAsField(e, p);
//		}
//
//		@Override
//		public Group visitExecutableAsMethod(ExecutableElement e, Void p) {
//			System.out.println(e);
//			return super.visitExecutableAsMethod(e, p);
//		}
		
		public void checkContext(TypeElement e) {
			System.out.println("Check context");
		}

		public void checkShellyAnotations(TypeElement e) {
			if(e.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT))
				return; // We do not check abstract classes
			for(Element elt: e.getEnclosedElements()) {
				if(containsOne(e))
					warn("%s has Shelly annotation but %s has no Shelly annotation", elt, e);
				if(elt instanceof TypeElement)
					checkShellyAnotations((TypeElement)elt);
			}
		}

		public void checkGroup(TypeElement e) {
			System.out.println("Check group");
		}
	}

	boolean containsOne(Element e) {
		for(AnnotationMirror c: e.getAnnotationMirrors())
			if(SHELLY_ANNOTATIONS.contains(c.getClass()))
					return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
	final List<Class<? extends Annotation>> SHELLY_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList((Class<? extends Annotation>[])new Class<?>[]{
			GROUP_CLASS, OPT_CLASS, CONTEXT_CLASS, CMD_CLASS,
			DESCRIPTION_CLASS, Param.class, Default.class, Error.class
	}));
	

	ElementModelFactory model_factory = new ElementModelFactory();
	class ElementModelFactory implements ModelFactory<TypeElement, Element> {


		public Group<TypeElement, Element> newGroup(String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			final boolean isDefault = clazz.getAnnotation(Default.class) != null;
			return new NavigableGroup(parent, name, null) {

				@Override
				public Action<TypeElement, Element> getDefault() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object newGroup(Object parent) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object getEnclosing(Object obj) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isDefault() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Description getDescription() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object apply(Object receive, String next, PeekIterator<String> _cmdline) {
					// TODO Auto-generated method stub
					return null;
				}
			} ;
		}

		@Override
		public Composite<TypeElement, Element> newContext(String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Command<TypeElement, Element> newCommand(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, String name, Element member) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Option<TypeElement, Element> newOption(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, String name, Element member) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	static abstract class NavigableGroup extends AbstractComposite<TypeElement, Element> implements Group<TypeElement, Element> {
		public NavigableGroup(Composite<TypeElement, Element> parent, String name, TypeElement clazz) {
			super(parent, name, clazz);
		}

		@Override
		public void accept(Visitor<TypeElement, Element> visitor) {
			visitor.visit(this);
		}

		@Override
		public Object createContext(Object parent) {
			return null;
		}
	}
	
	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.Context> CONTEXT_CLASS = fr.labri.shelly.annotations.Context.class;
	static public final Class<fr.labri.shelly.annotations.Group> GROUP_CLASS = fr.labri.shelly.annotations.Group.class;
	static public final Class<fr.labri.shelly.annotations.Description> DESCRIPTION_CLASS = fr.labri.shelly.annotations.Description.class;
}
