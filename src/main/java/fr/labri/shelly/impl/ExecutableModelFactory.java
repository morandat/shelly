package fr.labri.shelly.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import fr.labri.shelly.Command;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.ModelFactory;
import fr.labri.shelly.Option;
import fr.labri.shelly.Triggerable;
import fr.labri.shelly.impl.AnnotationUtils.ReflectValue;

public class ExecutableModelFactory implements ModelFactory<Class<?>, Member> {

	public static final ExecutableModelFactory EXECUTABLE_MODEL = new ExecutableModelFactory();

	public static class ReflectVisitor {
		public void visit(AnnotatedElement o) {
		}
		public void visit(Class<?> c) {
			visit((AnnotatedElement)c);
		}
		public void visit(Field c) {
			visit((AnnotatedElement)c);
		}
		public void visit(Method c) {
			visit((AnnotatedElement)c);
		}
		
		void visit_all(Class<?> clazz) {
			for (Field f : clazz.getFields())
				visit(f);
			for (Method m : clazz.getMethods())
				visit(m);
			for (Class<?> c : clazz.getClasses())
				visit(c);	
		}
	}
	
	static public class Executable extends ModelBuilder<Class<?>, Member> {
		@Override
		public ExecutableBuilder newBuilder() {
			return new ExecutableBuilder();
		}

		@Override
		public Group<Class<?>, Member> createModel(Class<?> clazz) {
			return createModel(clazz.getAnnotation(GROUP_CLASS), clazz);
		}

		static private class ExecutableBuilder extends ModelBuilder.Builder<Class<?>, Member> {
			ExecutableModelFactory _parentFactory = ExecutableModelFactory.EXECUTABLE_MODEL;
			ConverterFactory _parentConverter = fr.labri.shelly.impl.ConverterFactory.DEFAULT;

			@Override
			public void visit(Composite<Class<?>, Member> optionGroup) {
				ConverterFactory p = _parentConverter;
				super.visit(optionGroup);
				_parentConverter = p;
			}

			@Override
			protected void populate(final Composite<Class<?>, Member> grp) {
				Class<?> clazz = grp.getAssociatedElement();
				new ReflectVisitor() {
					public void visit(Class<?> c) {
						if (c.isAnnotationPresent(GROUP_CLASS)) {
							grp.addItem(createGroup(grp, c.getAnnotation(GROUP_CLASS), c));
						} else if (c.isAnnotationPresent(CONTEXT_CLASS)) {
							grp.addItem(createContext(grp, c.getAnnotation(CONTEXT_CLASS), c));
						}
					}
					
					public void visit(Field f) {
						if (f.isAnnotationPresent(OPT_CLASS)) {
							grp.addItem(createOption(f.getAnnotation(OPT_CLASS), f, grp));
						}
					}
					
					public void visit(Method m) {
						if (m.isAnnotationPresent(CMD_CLASS)) {
							grp.addItem(createCommand(m.getAnnotation(CMD_CLASS), m, grp));
						} else if (m.isAnnotationPresent(OPT_CLASS)) {
							grp.addItem(createOption(m.getAnnotation(OPT_CLASS), m, grp));
						}
					}


				}.visit_all(clazz);
			}
			
			protected ExecutableModelFactory getFactory(Class<? extends ExecutableModelFactory> factory) {
				return ExecutableModelFactory.loadModelFactory(_parentFactory, factory);
			}

			public Context<Class<?>, Member> createContext(Composite<Class<?>, Member> parent, fr.labri.shelly.annotations.Context annotation, Class<?> clazz) {
				if ((parent == null) != (clazz.getEnclosingClass() == null))
					throw new RuntimeException("Cannot create option group when not starting at top level");
				return super.createContext(parent, annotation, clazz);
			}

			public Group<Class<?>, Member> createGroup(Composite<Class<?>, Member> parent, fr.labri.shelly.annotations.Group annotation, Class<?> clazz) {
				if ((parent == null) != (clazz.getEnclosingClass() == null))
					throw new RuntimeException("Cannot create option group when not starting at top level"); // FIXME
				return super.createGroup(parent, annotation, clazz);
			}
			
			protected Option<Class<?>, Member> createOption(fr.labri.shelly.annotations.Option annotation, Member member, Composite<Class<?>, Member> parent) {
				if (member instanceof Constructor<?>)
					throw new RuntimeException("Cannot create option on constructors: "+ member);
				return super.createOption(annotation, member, parent);
			}

			protected Command<Class<?>, Member> createCommand(fr.labri.shelly.annotations.Command annotation, Member method, Composite<Class<?>, Member> parent) {
				if (!(method instanceof Method))
					throw new RuntimeException("Command are restricted to methods: "+ method);
				return super.createCommand(annotation, method, parent);
			}

			@Override
			protected ConverterFactory getConverterFactory(Class<? extends ConverterFactory>[] classes) {
//				if (classes.length < 1 || BasicConverter.class.equals(classes[0]))
				if(classes == null)
					return _parentConverter;
				return fr.labri.shelly.impl.ConverterFactory.getComposite(_parentConverter, classes);
			}

			@Override
			protected String getCName(Class<?> clazz) {
				return clazz.getSimpleName();
			}
			@Override
			protected String getMName(Member member) {
				return member.getName();
			}
		}
	}
	
	public interface OptionAdapter extends TriggerableAdapter {
		abstract Object setOption(Option<Class<?>, Member> opt, Object receive, Object value);
	}
	
	public interface CommandAdapter extends ActionAdapter {
		public abstract void executeCommand(AbstractCommand<Class<?>, Member> cmd, Object grp, Executor executor, String text);
	}
	
	public interface ActionAdapter extends TriggerableAdapter {
	}
	
	public interface TriggerableAdapter {
		public abstract Description getDescription(Triggerable<Class<?>, Member> cmd);
	}

	protected static ExecutableModelFactory loadModelFactory(ExecutableModelFactory _parentFactory, Class<? extends ExecutableModelFactory> newFactory) {
		ExecutableModelFactory factory = _parentFactory;
		if(newFactory != null)
			factory = factory.instantiateFactory(newFactory);
		return factory;
	}

	abstract static class CompositeAdapter {
		final Field _superThis;
		final Constructor<?> _ctor;

		CompositeAdapter(Class<?> clazz) {
			try {
				Constructor<?> ctor;
				if (clazz.getEnclosingClass() != null) {
					ctor = clazz.getConstructor(clazz.getEnclosingClass());
					_superThis = getSuperThisField(clazz);
					_superThis.setAccessible(true);
				} else {
					ctor = clazz.getConstructor();
					_superThis = null;
				}
				_ctor = ctor;
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		public Object instantiateObject(Object parent) {
			return ExecutableModelFactory.newInstance(_superThis != null, _ctor, parent);
		}

		public Object getEnclosingObject(Object obj) {
			try {
				return _superThis.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public static Field getSuperThisField(Class<?> c) {
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields)
				if (f.getName().startsWith("this$"))
					return f;
			throw new RuntimeException("This class has no enclosing class.\n" + Arrays.toString(fields));
		}
	}

	abstract static class ContextAdapter extends CompositeAdapter {
		ContextAdapter(Class<?> clazz) {
			super(clazz);
		}
	}
	abstract static class GroupAdapter extends CompositeAdapter implements ActionAdapter {
		GroupAdapter(Class<?> clazz) {
			super(clazz);
		}
		
		public abstract Object executeCommand(AbstractCommand<Class<?>, Member> cmd, Object grp, Executor executor, String text);

		@Override
		public Object instantiateObject(Object parent) {
			return ExecutableModelFactory.newInstance(_superThis != null, _ctor, parent);
		}
		
		public Object apply(AbstractGroup<Class<?>, Member> grp, Object receive, Executor executor) {
			return receive;
		}
	}

	public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz, final CompositeAdapter adapter) {
		return new AbstractContext<Class<?>, Member>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
			@Override
			public Object instantiateObject(Object parent) {
				return adapter.instantiateObject(parent);
			}

			@Override
			public Object getEnclosingObject(Object obj) {
				return adapter.getEnclosingObject(obj);
			}
		};
	}

	@Override
	public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz) {
		return newContext(name, parent, clazz, new CompositeAdapter(clazz){
		});
	}

	
	public Group<Class<?>, Member> newGroup(String name, Composite<Class<?>, Member> parent, final Class<?> clazz) {
		final GroupAdapter adapter = new GroupAdapter(clazz) {
			@Override
			public Object executeCommand(AbstractCommand<Class<?>, Member> cmd, Object grp, Executor executor, String text) {
				return grp;
			}

			@Override
			public Description getDescription(Triggerable<Class<?>, Member> group) {
				return DescriptionFactory.getGroupDescription((Group<Class<?>, Member>) group, clazz, SUMMARY.getGroup(clazz));
			}
		};
		
		return new AbstractGroup<Class<?>, Member>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
			@Override
			public void executeAction(Object receive, String next, Executor executor) {
				adapter.apply(this, receive, executor);
			}

			@Override
			public Object getEnclosingObject(Object parent) {
				return adapter.getEnclosingObject(parent);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription(this);
			}

			@Override
			public Object instantiateObject(Object parent) {
				return adapter.instantiateObject(parent);
			}
		};
	}

	static Object newInstance(boolean enclosed, Constructor<?> ctor, Object parent) {
		try {
			if (enclosed) {
				return ctor.newInstance(parent);
			} else {
				return ctor.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Option<Class<?>, Member> newOption(String name, Composite<Class<?>, Member> parent, Member member, final Converter<?> converter,
			final OptionAdapter adapter) {
		return new AbstractOption<Class<?>, Member>(parent, name, member, AnnotationUtils.extractAnnotation(member)) {
			@Override
			public void executeAction(Object receive, String next, Executor executor) {
				Object o = converter.convert(next, executor.getCommandLine());
				adapter.setOption(this, receive, o);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription(this);
			}
		};
	}

	public static Option<Class<?>, Member> newBooleanOption(String name, Composite<Class<?>, Member> parent, Member member, final OptionAdapter adapter) {
		return new AbstractOption<Class<?>, Member>(parent, name, member, AnnotationUtils.extractAnnotation(member)) {
			@Override
			public int isValid(Parser parser, String str, int index) {
				return parser.isLongBooleanOptionValid(str, this, index);
			}

			@Override
			public void executeAction(Object receive, String next, Executor executor) {
				adapter.setOption(this, receive, !executor.getParser().getBooleanValue(next));
			}

			@Override
				public Description getDescription() {
				return adapter.getDescription(this);
			}
		};
	}

	public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, final Member member) {
		if(member instanceof Field)
			return newOption(factory, parent, name, (Field)member);
		if(member instanceof Method)
			return newOption(factory, parent, name, (Method)member);
		throw new RuntimeException("Option cannot be associated with constructor: "+ member);
	}
	public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, Field field) {
		Class<?> type = field.getType();
		if (typeIsBool(type))
			return newBooleanOption(name, parent, field, getFieldAdapter(field));
		return newOption(name, parent, field, factory.getConverter(type, true, name), getFieldAdapter(field));
	}

	public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, final Method method) {
		Class<?> type = method.getParameterTypes()[0];
		if (typeIsBool(type))
			return newBooleanOption(name, parent, method, getAccessorAdapter(method));
		return newOption(name, parent, method, factory.getConverter(type, true, name), getAccessorAdapter(method));
	}

	boolean typeIsBool(Class<?> clazz) {
		return (Boolean.class.equals(clazz) || boolean.class.equals(clazz));
	}

	public Command<Class<?>, Member> newCommand(String name, Composite<Class<?>, Member> parent, Member member, Converter<?>[] converters,
			final CommandAdapter adapter) {
		return new AbstractCommand<Class<?>, Member>(name, parent, member, AnnotationUtils.extractAnnotation(member)) {
			@Override
			public void executeAction(Object receive,  String next, Executor executor) {
				adapter.executeCommand(this, receive, executor, next);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription(this);
			}
		};
	}
	
	public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Composite<Class<?>, Member> parent, String name, Member member) {
		return newCommand(loadFactory, parent, name, (Method)member); // FIXME check
	}
	
	public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Composite<Class<?>, Member> parent, String name, final Method method) {
		final Converter<?>[] converters = fr.labri.shelly.impl.ConverterFactory.getConverters(loadFactory, method.getParameterTypes(), method.getParameterAnnotations());
		return newCommand(name, parent, method, converters, new CommandAdapter() {
			@Override
			public void executeCommand(AbstractCommand<Class<?>, Member> cmd, Object grp, Executor executor, String text) {
				Object[] arguments = fr.labri.shelly.impl.ConverterFactory.convertArray(converters, text, executor.getCommandLine());
				try {
					method.invoke(grp, arguments);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription(Triggerable<Class<?>, Member> cmd) {
				return DescriptionFactory.getCommandDescription(method, SUMMARY.getCommand(method));
			}
		});
	}

	static OptionAdapter getFieldAdapter(final Field field) {
		return new OptionAdapter() {
			@Override
			public Object setOption(Option<Class<?>, Member> opt, Object receive, Object value) {
				try {
					field.set(receive, value);
					return value;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
				return DescriptionFactory.getDescription(field, SUMMARY.getOption(field));
			}
		};
	}

	static OptionAdapter getAccessorAdapter(final Method method) {
		return new OptionAdapter() { // FIXME not robust
			@Override
			public Object setOption(Option<Class<?>, Member> opt, Object receive, Object value) {
				try {
					method.invoke(receive, value);
					return value;
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

			public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
				return DescriptionFactory.getDescription(method, SUMMARY.getOption(method));
			}
		};
	}

	static class AbstractModelFactory extends ExecutableModelFactory {
		final ExecutableModelFactory _parent;

		public AbstractModelFactory(ExecutableModelFactory parent) {
			_parent = parent;
		}

		@Override
		public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, Field field) {
			return _parent.newOption(factory, parent, name, field);
		}

		@Override
		public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, Method method) {
			return _parent.newOption(factory, parent, name, method);
		}

		@Override
		public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Composite<Class<?>, Member> parent, String name, Method method) {
			return _parent.newCommand(loadFactory, parent, name, method);
		}

		@Override
		public Command<Class<?>, Member> newCommand(String name, Composite<Class<?>, Member> parent, Member member, Converter<?>[] converters,
				CommandAdapter adapter) {
			return _parent.newCommand(name, parent, member, converters, adapter);
		}

		@Override
		public Group<Class<?>, Member> newGroup(String name, Composite<Class<?>, Member> parent, Class<?> clazz) {
			return _parent.newGroup(name, parent, clazz);
		}

		@Override
		public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz,
				CompositeAdapter adapter) {
			return _parent.newContext(name, parent, clazz, adapter);
		}
	}

	ExecutableModelFactory instantiateFactory(Class<? extends ExecutableModelFactory> clazz) {
		try {
			Constructor<? extends ExecutableModelFactory> c = clazz.getConstructor(ExecutableModelFactory.class);
			if (c != null)
				return c.newInstance(this);
			else
				return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return EXECUTABLE_MODEL;
		}
	}
	
	public final static ReflectValue<String> SUMMARY = new ReflectValue<String>("summary", fr.labri.shelly.annotations.Option.NO_NAME);
};