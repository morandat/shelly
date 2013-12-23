package fr.labri.shelly.doctor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor7;
import javax.tools.Diagnostic.Kind;

import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Context;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.Item;
import fr.labri.shelly.ModelFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.Terminal;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.impl.AbstractGroup;
import fr.labri.shelly.impl.AbstractContext;
import fr.labri.shelly.impl.AbstractCommand;
import fr.labri.shelly.impl.AbstractItem;
import fr.labri.shelly.impl.AbstractOption;
import fr.labri.shelly.impl.AnnotationUtils;
import fr.labri.shelly.impl.DescriptionFactory;
import fr.labri.shelly.impl.ExecutableModelFactory;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.ModelBuilder;
import fr.labri.shelly.impl.VisitorAdapter;
import fr.labri.shelly.impl.AnnotationUtils.ElementValue;
import static fr.labri.shelly.ModelFactory.*;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes( "fr.labri.shelly.annotations.*" )
@SupportedOptions({Doctor.STRICT, Doctor.DEBUG})
public class Doctor extends AbstractProcessor {
	
	public static final String STRICT = "strict";
	public static final String DEBUG = "debug";

	private Messager messager;
	private boolean isDebug;
	private boolean isStrict;

	ElementModelBuilder model_builder = new ElementModelBuilder();
	ElementModelFactory model_factory = new ElementModelFactory();
	
	TypeMirror[] error_signature;
	
	String getOption(String key, String dflt) {
		Map<String, String> options = processingEnv.getOptions();
		if(options.containsKey(key))
			return options.get(key);
		return dflt;
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		messager = processingEnv.getMessager();
		isDebug = Boolean.parseBoolean(getOption(DEBUG, "false"));
		isStrict = Boolean.parseBoolean(getOption(STRICT, "false"));

		error_signature = new TypeMirror[] {
				processingEnv.getElementUtils().getTypeElement("java.lang.Exception").asType(),
				processingEnv.getTypeUtils().getArrayType(processingEnv.getElementUtils().getTypeElement("java.lang.String").asType())
		};		
		for (Element elt : roundEnv.getRootElements()) {
			debug(elt, "Doctor is building class");
			Group<TypeElement, Element> model = createModel(elt);
			debug("Doctor is cheking names in %s mode", isStrict ? "strict" : "non strict");
			checkModel(model, isStrict);
		}
		return true;
	}

	public Group<TypeElement, Element> createModel(Element elt) {
		if (!(elt instanceof TypeElement))
			messager.printMessage(Kind.ERROR, "Root element is not a class", elt);
		Group<TypeElement, Element> grp = model_builder.createModel((TypeElement) elt);
//		grp.startVisit(new PrintHelpVisitor());

		return grp;
	}

	class PrintHelpVisitor extends VisitorAdapter.TraversalVisitor<TypeElement, Element> {
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
	}
	

	protected void debug(Element elt, String msg, Object... args) {
		if(isDebug)
			processingEnv.getMessager().printMessage(Kind.NOTE, String.format(msg, args), elt);
	}
	
	protected void debug(String msg, Object... args) {
		if(isDebug)
			debug(null, msg, args);
	}

	private void checkModel(Group<TypeElement, Element> model, final boolean strict) {
		new VisitorAdapter<TypeElement, Element>() {
			ArrayList<Map<String, Element>> _options = new ArrayList<>();
			ArrayList<Map<String, Element>> _commands = new ArrayList<>();
			
			public void start(Group<TypeElement, Element> model) {
				_commands.add(new HashMap<String, Element>());
				_options.add(new HashMap<String, Element>());
				visit(model);
			}
			
			VisitorAdapter<TypeElement, Element> action_visitor = new VisitorAdapter<TypeElement, Element>() {
				@Override
				public void visit(Command<TypeElement, Element> item) {
					addItem(item, item.getAssociatedElement(), _commands, Kind.ERROR);
				}
				@Override
				public void visit(Group<TypeElement, Element> item) {
					addItem(item, item.getAssociatedElement(), _commands, Kind.ERROR);
				}
			};
			
			VisitorAdapter<TypeElement, Element> option_visitor = new VisitorAdapter<TypeElement, Element>() {
				@Override
				public void visit(Option<TypeElement, Element> item) {
					addItem(item, item.getAssociatedElement(), _options, Kind.WARNING);
				}
			};

			@Override
			public void visit(Composite<TypeElement, Element> item) {
				_options.add(new HashMap<String, Element>());
				item.visit_all(option_visitor);
				item.visit_all(this);
				_options.remove(_options.size() - 1);
			}
			
			@Override
			public void visit(Group<TypeElement, Element> grp) {
				addItem(grp, grp.getAssociatedElement(), _commands, Kind.ERROR);
				
				ArrayList<Map<String, Element>> options = null;
				ArrayList<Map<String, Element>> commands = _commands; 
				(_commands = new ArrayList<>()).add(new HashMap<String, Element>());
				if(strict)
					options = _options;
				action_visitor.visit_actions(grp);
				visit((Composite<TypeElement, Element>) grp);
				if(strict)
					_options = options;
				_commands = commands;
			}
			
			private void addItem(Item<TypeElement, Element> item, Element e,  ArrayList<Map<String,Element>> stack, Kind error) {
				String name = item.getID();
				Element found = getByName(name, stack);
				Map<String, Element> last = stack.get(stack.size() - 1);
				
				if(found != null) {
					error = last.containsKey(name) ? Kind.ERROR : error;
					messager.printMessage(error, String.format("%s '%s' is hidding %s '%s'", item, AbstractItem.getFullName(item), found, e), e);
				}

				last.put(name, e);
			}
			
			private Element getByName(String name, ArrayList<Map<String, Element>> stack) {
				for(int i = stack.size(); i > 0 ; i --) {
					Map<String, Element> names = stack.get(i - 1);
					if(names.containsKey(name))
						return names.get(name);
				}
				return null;
			}
		}.start(model);
	}

	class ElementModelBuilder extends ModelBuilder<TypeElement, Element> {
		@Override
		public Group<TypeElement, Element> createModel(TypeElement clazz) {
			return createModel(clazz.getAnnotation(GROUP_CLASS), clazz);
		}

		@Override
		public ElementBuilder newBuilder() {
			return new ElementBuilder();
		}
		
		boolean containsOne(Element e) {
			for (AnnotationMirror c : e.getAnnotationMirrors())
				if (ModelFactory.SHELLY_ANNOTATIONS.contains(c.getClass()))
					return true;
			return false;
		}

		public void checkShellyUnreacheable(TypeElement e) {
			e.accept(new ElementKindVisitor7<Void, Void> () {
				@Override
				public Void visitTypeAsClass(TypeElement e, Void p) {
					if (e.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT))
						return DEFAULT_VALUE; // We do not check abstract classes
					return super.visitTypeAsClass(e, p);
				}

				public void checkAnnotations(Element e) {
					for(Class<? extends Annotation> a: SHELLY_ANNOTATIONS)
						if(e.getAnnotation(a) != null)
							messager.printMessage(Kind.ERROR, String.format("Unreachable shelly annotation %s", a.getSimpleName()), e);
				}
				
				@Override
				protected Void defaultAction(Element e, Void p) {
					checkAnnotations(e);
					
					for(Element ee: e.getEnclosedElements())
						ee.accept(this, p);
					return DEFAULT_VALUE;
				}
				
			}, null);
			for (Element elt : e.getEnclosedElements()) {
				if (containsOne(e))
					messager.printMessage(Kind.WARNING, String.format("%s has Shelly annotation but %s has no Shelly annotation", elt, e), elt);
				if (elt instanceof TypeElement)
					checkShellyUnreacheable((TypeElement) elt);
			}
		}

		class ElementBuilder extends Builder<TypeElement, Element> {
			@Override
			protected ModelFactory<TypeElement, Element> getFactory(Class<? extends ExecutableModelFactory> parent) {
				return model_factory;
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

			@Override
			protected Group<TypeElement, Element> createGroup(Composite<TypeElement, Element> parent, fr.labri.shelly.annotations.Group annotation,
					TypeElement clazz) {
				checkType(clazz, ElementKind.CLASS);
				checkAccessibility(clazz);
				return super.createGroup(parent, annotation, clazz);
			}

			@Override
			protected Context<TypeElement, Element> createContext(Composite<TypeElement, Element> parent, fr.labri.shelly.annotations.Context annotation,
					TypeElement clazz) {
				checkType(clazz, ElementKind.CLASS);
				checkAccessibility(clazz);
				return super.createContext(parent, annotation, clazz);
			}

			@Override
			protected Option<TypeElement, Element> createOption(fr.labri.shelly.annotations.Option annotation, Element member,
					Composite<TypeElement, Element> parent) {
				checkType(member, ElementKind.FIELD, ElementKind.METHOD);
				if(ElementKind.METHOD == member.getKind()) {
					if(((ExecutableElement)member).getParameters().size() != 1) {
						messager.printMessage(Kind.WARNING, "Option on method is only allowed for accessors, i.e., exactly one parameter", member); // FIXME should be an ERROR when there is no factory set
					}
				} else  if (member.getModifiers().contains(Modifier.FINAL))
					messager.printMessage(Kind.ERROR, "Final fields can't be annotated by @Option", member);
				checkAccessibility(member);
				return super.createOption(annotation, member, parent);
			}

			@Override
			protected Command<TypeElement, Element> createCommand(fr.labri.shelly.annotations.Command annotation, Element member,
					Composite<TypeElement, Element> parent) {
				checkAccessibility(member);
				return super.createCommand(annotation, member, parent);
			}
			

			class CreateItem extends ElementKindVisitor7<Void, Composite<TypeElement, Element>> {
				@Override
				public Void visitTypeAsClass(TypeElement e, Composite<TypeElement, Element> p) {
					if (e.getAnnotation(GROUP_CLASS) != null)
						p.addItem(createGroup(p, e.getAnnotation(GROUP_CLASS), e));
					else if (e.getAnnotation(CONTEXT_CLASS) != null)
						p.addItem(createContext(p, e.getAnnotation(CONTEXT_CLASS), e));
					else
						checkShellyUnreacheable(e);
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
					else if (e.getAnnotation(ERROR_CLASS) != null)
						checkError(e.getAnnotation(ERROR_CLASS), e, p);
					return defaultAction(e, p);
				}
			}
		}
	}
	
	class ElementModelFactory implements ModelFactory<TypeElement, Element> {
		public Group<TypeElement, Element> newGroup(final String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			return new AbstractGroup<TypeElement, Element>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
				@Override
				public Description getDescription() {
					return DescriptionFactory.getGroupDescription(this, _clazz, SUMMARY.getGroup(_clazz));
				}
				@Override
				public boolean isEnclosed() {
					return Doctor.isStatic(_clazz);
				}
			};
		}

		@Override
		public Context<TypeElement, Element> newContext(String name, Composite<TypeElement, Element> parent, TypeElement clazz) {
			return new AbstractContext<TypeElement, Element>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
				@Override
				public boolean isEnclosed() {
					return Doctor.isStatic(_clazz);
				}
			};
		}

		@Override
		public Command<TypeElement, Element> newCommand(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, final String name,
				final Element member) {
			return new AbstractCommand<TypeElement, Element>(name, parent, member, AnnotationUtils.extractAnnotation(member)) {
				@Override
				public Description getDescription() {
					return DescriptionFactory.getDescription(member, name);
				}
			};
		}

		@Override
		public Option<TypeElement, Element> newOption(ConverterFactory loadFactory, Composite<TypeElement, Element> parent, final String name,
				final Element member) {
			return new AbstractOption<TypeElement, Element>(parent, name, member, AnnotationUtils.extractAnnotation(member)) {
				@Override
				public Description getDescription() {
					return DescriptionFactory.getDescription(member, name);
				}
			};
		}
	};
	
	void checkType(Element e, ElementKind... kinds) {
		ElementKind k = e.getKind();
		for(ElementKind ks: kinds)
			if(ks == k)
				return;
		messager.printMessage(Kind.ERROR, String.format("Shelly item is not allowed on this kind of element %s. Allowed on %s", k, Arrays.toString(kinds)), e);
	}
	
	public boolean checkSignature(TypeMirror[] expected, List<? extends VariableElement> actual) {
		if(expected.length != actual.size())
			return false;
		Iterator<? extends VariableElement> it = actual.iterator();
		for(TypeMirror ec: expected) {
			TypeMirror a = it.next().asType(); 
			if(!processingEnv.getTypeUtils().isSubtype(ec, a))
				return false;
		}
		return true;
	}
	
	void checkAccessibility(Element member) {
		if(!member.getModifiers().contains(Modifier.PUBLIC))
			messager.printMessage(Kind.ERROR, "Can't create non public item", member);
		if(member.getModifiers().contains(Modifier.ABSTRACT))
			messager.printMessage(Kind.ERROR, "Can't create abstract item", member);
	}

	public void checkError(Error error, ExecutableElement e, Composite<TypeElement, Element> p) {
		checkAccessibility(e);
		if(!checkSignature(error_signature, e. getParameters()))
			messager.printMessage(Kind.ERROR, String.format("Error methods should have for parameters : %s", Arrays.toString(error_signature)), e);
	}
	
	static boolean isStatic(TypeElement clazz) {
		return clazz.getModifiers().contains(Modifier.STATIC);
	}
	
	public final static ElementValue<String> SUMMARY = new ElementValue<String>("summary", fr.labri.shelly.annotations.Option.NO_NAME);
}
