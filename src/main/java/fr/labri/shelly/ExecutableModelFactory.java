package fr.labri.shelly;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import fr.labri.shelly.Group.AbstractGroup;
import fr.labri.shelly.Option.AbstractOption;
import fr.labri.shelly.Command.AbstractCommand;
import fr.labri.shelly.Context.AbstractContext;
import fr.labri.shelly.Converter.SimpleConverter;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Param;
import fr.labri.shelly.annotations.AnnotationUtils.AnnotationType;
import fr.labri.shelly.annotations.AnnotationUtils.AnnotationValue;

public class ExecutableModelFactory implements ModelFactory<Class<?>, Member> {

	public static class ReflectVisitor {
		public void visit(AnnotatedElement o) {
		}

		public void visit(Class<?> c) {
			visit((AnnotatedElement) c);
		}

		public void visit(Field c) {
			visit((AnnotatedElement) c);
		}

		public void visit(Method c) {
			visit((AnnotatedElement) c);
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
		public Group<Class<?>, Member> createModel(Class<?> clazz) {
			return createModel(clazz.getAnnotation(GROUP_CLASS), clazz);
		}

		ExecutableModelFactory _parentFactory = new ExecutableModelFactory();
		ConverterFactory _parentConverter = DEFAULT;

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
					}
					if (c.isAnnotationPresent(CONTEXT_CLASS)) {
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
					}
					if (m.isAnnotationPresent(OPT_CLASS)) {
						grp.addItem(createOption(m.getAnnotation(OPT_CLASS), m, grp));
					}
				}
			}.visit_all(clazz);
		}

		protected ExecutableModelFactory getFactory(Class<? extends ExecutableModelFactory> factory) {
			return loadModelFactory(_parentFactory, factory);
		}

		public Context<Class<?>, Member> createContext(Composite<Class<?>, Member> parent, fr.labri.shelly.annotations.Context annotation, Class<?> clazz) {
			if ((parent == null) && (clazz.isMemberClass()))
				throw new RuntimeException("Cannot create context when not starting at top level");
			return super.createContext(parent, annotation, clazz);
		}

		protected Option<Class<?>, Member> createOption(fr.labri.shelly.annotations.Option annotation, Member member, Composite<Class<?>, Member> parent) {
			if (member instanceof Constructor<?>)
				throw new ShellyException("Cannot create option on constructors: " + member);
			if (member instanceof Field && Modifier.isFinal(member.getModifiers()))
				throw new ShellyException("Cannot create option on final fields");
			return super.createOption(annotation, member, parent);
		}

		protected Command<Class<?>, Member> createCommand(fr.labri.shelly.annotations.Command annotation, Member method, Composite<Class<?>, Member> parent) {
			if (!(method instanceof Method))
				throw new ShellyException("Command are restricted to methods: " + method);
			return super.createCommand(annotation, method, parent);
		}

		@Override
		protected ConverterFactory getConverterFactory(Class<? extends ConverterFactory>[] classes) {
			// if (classes.length < 1 || BasicConverter.class.equals(classes[0]))
			if (classes == null)
				return _parentConverter;
			return getComposite(_parentConverter, classes);
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

	public interface OptionAdapter extends TriggerableAdapter {
		public void executeOption(Option<Class<?>, Member> opt, Object receive, Executor executor, String text);

		public int isValidLongOption(Option<Class<?>, Member> opt, Recognizer recognizer, String str, int index);
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
		if (newFactory != null)
			factory = factory.instantiateFactory(newFactory);
		return factory;
	}

	abstract static class CompositeAdapter {
		final Constructor<?> _ctor;
		final boolean isEnclosed;

		CompositeAdapter(Class<?> clazz) {
			try {
				Constructor<?> ctor;
				isEnclosed = clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
				if (isEnclosed) {
					ctor = clazz.getConstructor(clazz.getEnclosingClass());
				} else {
					ctor = clazz.getConstructor();
				}
				_ctor = ctor;
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		public Object instantiateObject(Composite<Class<?>, Member> item, Object parent) {
			return newInstance(item, _ctor, parent);
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

		abstract void executeGroup(AbstractGroup<Class<?>, Member> cmd, Object grp, Executor executor, String text);
	}

	static Cache<ConverterFactory> cache = new ExecutableModelFactory.Cache<ConverterFactory>() {
		@Override
		ConverterFactory newItem(Class<ConverterFactory> clazz) throws InstantiationException, IllegalAccessException {
			return clazz.newInstance();
		}
	};

	final static public ConverterFactory DEFAULT = new ConverterFactory.BasicConverters();

	public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz, final CompositeAdapter adapter) {
		return new AbstractContext<Class<?>, Member>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
			@Override
			public Object instantiateObject(Object parent) {
				return adapter.instantiateObject(this, parent);
			}

			@Override
			public boolean isEnclosed() {
				return ExecutableModelFactory.isEnclosed(_clazz);
			}
		};
	}

	@Override
	public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz) {
		return newContext(name, parent, clazz, new ContextAdapter(clazz) {
		});
	}

	public Group<Class<?>, Member> newGroup(String name, Composite<Class<?>, Member> parent, final Class<?> clazz) {
		return new AbstractGroup<Class<?>, Member>(parent, name, clazz, AnnotationUtils.extractAnnotation(clazz)) {
			private Description _description;
			final GroupAdapter adapter = new GroupAdapter(clazz) {

				@Override
				public void executeGroup(AbstractGroup<Class<?>, Member> cmd, Object grp, Executor executor, String text) {
				}

				@Override
				public Description getDescription(Triggerable<Class<?>, Member> group) {
					return _description = Description.Factory.getGroupDescription((Group<Class<?>, Member>) group, clazz, SUMMARY.getGroup(clazz));
				}
			};

			@Override
			public void execute(Object receive, String next, Executor executor) {
				adapter.executeGroup(this, receive, executor, next);
			}

			@Override
			public Description getDescription() {
				if (_description != null)
					return _description;
				return (_description = adapter.getDescription(this));
			}

			@Override
			public Object instantiateObject(Object parent) {
				return adapter.instantiateObject(this, parent);
			}

			@Override
			public boolean isEnclosed() {
				return ExecutableModelFactory.isEnclosed(_clazz);
			}
		};
	}

	static Object newInstance(Composite<Class<?>, Member> item, Constructor<?> ctor, Object parent) {
		try {
			if (item.isEnclosed()) {
				return ctor.newInstance(parent);
			} else {
				return ctor.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Option<Class<?>, Member> newOption(String name, Composite<Class<?>, Member> parent, Member member, final OptionAdapter adapter) {
		return new AbstractOption<Class<?>, Member>(parent, name, member, AnnotationUtils.extractAnnotation(member)) {
			private Description _description;

			@Override
			public int isValidLongOption(Recognizer recognizer, String str, int index) {
				return adapter.isValidLongOption(this, recognizer, str, index);
			}

			@Override
			public void execute(Object receive, String next, Executor executor) {
				adapter.executeOption(this, receive, executor, next);
			}

			@Override
			public Description getDescription() {
				if (_description != null)
					return _description;
				return (_description = adapter.getDescription(this));
			}
		};
	}

	public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, final Member member) {
		OptionAdapter adapter;
		if (member instanceof Field)
			adapter = getFieldAdapter(factory, (Field) member);
		else if (member instanceof Method)
			adapter = getAccessorAdapter(factory, (Method) member);
		else
			throw new RuntimeException("Option cannot be associated with constructor: " + member);
		return newOption(name, parent, member, adapter);
	}

	boolean isBoolean(Class<?> clazz) {
		return (Boolean.class.equals(clazz) || boolean.class.equals(clazz));
	}

	public Command<Class<?>, Member> newCommand(String name, Composite<Class<?>, Member> parent, Member member, Converter<?> converter,
			final CommandAdapter adapter) {
		return newCommand(name, parent, member, new Converter[] { converter }, adapter);
	}

	public Command<Class<?>, Member> newCommand(String name, Composite<Class<?>, Member> parent, Member member, Converter<?>[] converters,
			final CommandAdapter adapter) {
		return new AbstractCommand<Class<?>, Member>(name, parent, member, AnnotationUtils.extractAnnotation(member)) {
			private Description _description;

			@Override
			public void execute(Object receive, String next, Executor executor) {
				adapter.executeCommand(this, receive, executor, next);
			}

			@Override
			public Description getDescription() {
				if (_description != null)
					return _description;
				return (_description = adapter.getDescription(this));
			}
		};
	}

	public Command<Class<?>, Member> newCommand(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, Member member) {
		return newCommand(factory, parent, name, (Method) member); // FIXME check
	}

	public Command<Class<?>, Member> newCommand(ConverterFactory converterFactory, Composite<Class<?>, Member> parent, String name, final Method method) {
		final Converter<?>[] converters = getConverters(converterFactory, method.getParameterTypes(), method.getParameterAnnotations());
		return newCommand(name, parent, method, converters, new CommandAdapter() {
			@Override
			public void executeCommand(AbstractCommand<Class<?>, Member> cmd, Object grp, Executor executor, String text) {
				Object[] arguments = convertArray(text, converters, executor);
				try {
					method.invoke(grp, arguments);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription(Triggerable<Class<?>, Member> cmd) {
				return Description.Factory.getCommandDescription(method, SUMMARY.getCommand(method));
			}
		});
	}

	static OptionAdapter getFieldAdapter(ConverterFactory converterFactory, final Field field) {
		Class<?> type = field.getType();
		if (type.isArray()) {
			return getFieldArrayAdapter(converterFactory, field);
		} else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
			return getBooleanFieldAdapter(field);
		} else if (Collection.class.isAssignableFrom(type)) {
			return getFieldCollectionAdapter(converterFactory, field);
		} else if (Map.class.isAssignableFrom(type)) {
			return getFieldMapAdapter(converterFactory, field);
		}
		return getDirectAdapter(converterFactory, field);
	}

	static OptionAdapter getFieldArrayAdapter(ConverterFactory converterFactory, final Field field) {
		return new FieldNearlyCollectionOptionAdapter<Object[]>(converterFactory, field) {
			protected Object[] newCollection() {
				return (Object[]) Array.newInstance(type(field), 0);
			}

			@Override
			protected Class<?> type(Field field) {
				return field.getType().getComponentType();
			}

			@Override
			protected void add(Object recv, Object[] array, Object converted) throws IllegalArgumentException, IllegalAccessException {// TODO rewrite to avoid some array allocations
				int len = array.length;
				Object[] source = array;
				Class<?> type = array.getClass().getComponentType();
				array = (Object[]) Array.newInstance(type, len + 1);
				array[len] = converted;
				System.arraycopy(source, 0, array, 0, len);
				field.set(recv, array);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static final Class<? extends Collection<?>>[] COLLECTION_TYPES = (Class<? extends Collection<?>>[]) new Class<?>[] {
			ArrayList.class,
			Vector.class,
			HashSet.class
	};

	@SuppressWarnings("unchecked")
	public static final Class<? extends Map<?, ?>>[] MAP_TYPES = (Class<? extends Map<?, ?>>[]) new Class<?>[] {
			LinkedHashMap.class,
			HashMap.class
	};

	public static OptionAdapter getMap(Type type, Class<?> targetClass) {
		ParameterizedType pt = (ParameterizedType) type;
		Class<?> clazz = (Class<?>) (pt.getRawType());
		Type[] at = pt.getActualTypeArguments();
		if (!(at.length == 2 || at[0] instanceof Class && at[1] instanceof Class))
			throw new RuntimeException("Map key/val types must (currently??) be classes");
		Class<?> key = (Class<?>) at[0];
		Class<?> val = (Class<?>) at[1];

		if (targetClass.isAssignableFrom(clazz)) {
			for (Class<? extends Collection<?>> c : COLLECTION_TYPES) {
				if (clazz.isAssignableFrom(c)) {
					return getFieldMapAdapter(c, key, val);
				}
			}
		}
		return null;
	}

	static OptionAdapter getFieldMapAdapter(Class<? extends Collection<?>> c, Class<?> key, Class<?> val) {
		return null; // TODO
	}

	public static <E> OptionAdapter getFieldMapAdapter(ConverterFactory factory, Field field) {
		Class<?> clazz = field.getType();
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType))
			throw new RuntimeException("Map should have a two type declared and be " + Arrays.toString(MAP_TYPES) + " or a super type of those");
		Class<? extends Map<?, ?>> c = findClass(clazz, MAP_TYPES);
		if (c == null)
			return new FieldOptionMapAdapter(factory, field);
		else
			return getFieldNewableMapAdapter(factory, field, c);
	}

	public static <E> OptionAdapter getFieldCollectionAdapter(ConverterFactory factory, Field field) {
		Class<?> clazz = field.getType();
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType))
			throw new RuntimeException("Collections should have a single type declared and be " + Arrays.toString(COLLECTION_TYPES)
					+ " or a super type of those");
		Class<? extends Collection<?>> c = findClass(clazz, COLLECTION_TYPES);
		if (c == null)
			return new FieldOptionCollectionAdapter(factory, field);
		else
			return getFieldNewableCollectionAdapter(factory, field, c);
	}

	static <E> Class<? extends E> findClass(Class<?> clazz, Class<? extends E>[] list) {
		for (Class<? extends E> c : list) {
			if (clazz.isAssignableFrom(c)) {
				return c;
			}
		}
		return null;
	}

	static OptionAdapter getFieldNewableMapAdapter(ConverterFactory factory, Field field, final Class<? extends Map<?, ?>> c) {
		return new FieldOptionMapAdapter(factory, field) {
			@SuppressWarnings("unchecked")
			@Override
			protected Map<Object, Object> newCollection() {
				try {
					return (Map<Object, Object>) c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Cannot instantiate collection associated with this object");
				}
			}
		};
	}

	static OptionAdapter getFieldNewableCollectionAdapter(ConverterFactory factory, Field field, final Class<? extends Collection<?>> c) {
		return new FieldOptionCollectionAdapter(factory, field) {
			@SuppressWarnings("unchecked")
			@Override
			protected Collection<Object> newCollection() {
				try {
					return (Collection<Object>) c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Cannot instantiate collection associated with this object");
				}
			}
		};
	}

	static abstract class FieldNearlyCollectionOptionAdapter<C> extends FieldOptionAdapter {
		public FieldNearlyCollectionOptionAdapter(ConverterFactory factory, Field f) {
			super(factory, f);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void set(Object receiver, Object converted) {
			try {
				C array = (C) field.get(receiver);
				if (array == null) {
					array = newCollection();
					field.set(receiver, array);
				}
				add(receiver, array, converted);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		abstract protected void add(Object recv, C array, Object converted) throws IllegalArgumentException, IllegalAccessException;

		protected C newCollection() {
			throw new RuntimeException("There is no default constructor for this type, you should instanciate it yourself");
		}
	}

	static class FieldOptionCollectionAdapter extends FieldNearlyCollectionOptionAdapter<Collection<Object>> {
		public FieldOptionCollectionAdapter(ConverterFactory factory, Field f) {
			super(factory, f);
		}

		protected void add(Object recv, Collection<Object> array, Object converted) {
			array.add(converted);
		}

		protected Class<?> type(Field field) {
			return findBound((ParameterizedType) field.getGenericType());
		}

		protected Class<?> findBound(ParameterizedType type) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] at = pt.getActualTypeArguments();
			if (at.length != 1 && !(at[0] instanceof Class))
				throw new RuntimeException("Collection type must (currently??) be classes");

			return (Class<?>) at[0];
		}
	}

	static class FieldOptionMapAdapter extends FieldNearlyCollectionOptionAdapter<Map<Object, Object>> {
		Converter<?>[] converters = new Converter<?>[2];

		public FieldOptionMapAdapter(ConverterFactory factory, Field f) {
			super(factory, f);
			converters = getConverters(factory, findBound((ParameterizedType) field.getGenericType()));
		}

		protected Class<?> type(Field field) {
			return Map.Entry.class;
		}

		protected Class<?>[] findBound(ParameterizedType type) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] at = pt.getActualTypeArguments();
			if (!(at.length == 2 || at[0] instanceof Class || at[0] instanceof Class))
				throw new RuntimeException("Map type must (currently??) be classes");

			return new Class<?>[] { (Class<?>) at[0], (Class<?>) at[1] };
		}

		@Override
		protected void add(Object recv, Map<Object, Object> map, Object converted) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> vals = (Map.Entry<String, String>) converted;
			map.put(((SimpleConverter<?>) converters[0]).convert(vals.getKey()), // FIXME ugly and unsafe cast
					((SimpleConverter<?>) converters[1]).convert(vals.getValue()));
		}
	}

	static class FieldOptionAdapter implements OptionAdapter {
		final Field field;
		final Converter<?> converter;

		public FieldOptionAdapter(ConverterFactory factory, Field f) {
			field = f;
			converter = factory.getConverter(type(field));
		}

		protected Class<?> type(Field field) {
			return field.getType();
		}

		@Override
		public void executeOption(Option<Class<?>, Member> opt, Object receive, Executor executor, String text) {
			try {
				set(receive, converter.convert(executor));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		protected void set(Object receive, Object converted) throws IllegalArgumentException, IllegalAccessException {
			field.set(receive, converted);
		}

		@Override
		public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
			return Description.Factory.getDescription(field, SUMMARY.getOption(field));
		}

		@Override
		public int isValidLongOption(Option<Class<?>, Member> opt, Recognizer recognizer, String str, int index) {
			return recognizer.isLongOptionValid(str, opt);
		}
	};

	static OptionAdapter getDirectAdapter(ConverterFactory converterFactory, final Field field) {
		return new FieldOptionAdapter(converterFactory, field) {
		};
	}

	static abstract class BooleanOptionAdapter implements OptionAdapter {
		protected boolean value(Option<?, ?> opt, Executor executor, String text) throws IllegalArgumentException, IllegalAccessException {
			return executor.getRecognizer().getBooleanValue(text, opt);
		}

		@Override
		public int isValidLongOption(Option<Class<?>, Member> opt, Recognizer recognizer, String str, int index) {
			return recognizer.isLongBooleanOptionValid(str, opt);
		}
	}

	static OptionAdapter getBooleanFieldAdapter(final Field field) {
		return new BooleanOptionAdapter() {
			@Override
			public void executeOption(Option<Class<?>, Member> opt, Object receive, Executor executor, String text) {
				try {
					field.set(receive, value(opt, executor, text));

				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
				return Description.Factory.getDescription(field, SUMMARY.getOption(field));
			}
		};
	}

	static OptionAdapter getAccessorAdapter(ConverterFactory converterFactory, final Method method) {
		Class<?>[] pt = method.getParameterTypes();
		if (pt.length > 0 && Boolean.class.isAssignableFrom(pt[0])) {
			return new OptionBooleanFieldAccessor(converterFactory, method);
		}
		return new OptionFieldAccessorAdapter(converterFactory, method);
	}

	static class OptionBooleanFieldAccessor extends BooleanOptionAdapter {
		final Method method;
		final Converter<?> converters[];

		public OptionBooleanFieldAccessor(ConverterFactory factory, Method method) {
			this.method = method;
			Class<?>[] types = method.getParameterTypes();
			Class<?>[] params = Arrays.copyOfRange(types, 1, types.length);
			converters = getConverters(factory, params);
		}

		public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
			return Description.Factory.getDescription(method, SUMMARY.getOption(method));
		}

		@Override
		public void executeOption(Option<Class<?>, Member> opt, Object receive, Executor executor, String text) {
			Object[] params = new Object[converters.length + 1];
			try {
				params[0] = value(opt, executor, text);
				if (converters.length > 0) {
					for (int i = 0; i < converters.length; i++)
						params[i + 1] = converters[i].convert(executor);
				}

				method.invoke(receive, params);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
	}

	static class OptionFieldAccessorAdapter implements OptionAdapter {
		final Method method;
		final Converter<?> converters[];

		public OptionFieldAccessorAdapter(ConverterFactory converterFactory, Method method) {
			this.method = method;
			converters = getConverters(converterFactory, method.getParameterTypes(), method.getParameterAnnotations());
		}

		@Override
		public void executeOption(Option<Class<?>, Member> opt, Object receive, Executor executor, String text) {
			try {
				method.invoke(receive, convertArray(text, converters, executor));
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		public Description getDescription(Triggerable<Class<?>, Member> abstractOption) {
			return Description.Factory.getDescription(method, SUMMARY.getOption(method));
		}

		public int isValidLongOption(Option<Class<?>, Member> opt, Recognizer recognizer, String str, int index) {
			return recognizer.isLongOptionValid(str, opt);
		}

		public boolean isValidShortOption(Option<Class<?>, Member> opt, Recognizer recognizer, char flag, int index) {
			return recognizer.isShortOptionValid(flag, opt);
		}
	}

	public static class AbstractModelFactory extends ExecutableModelFactory {
		final ExecutableModelFactory _parent;

		public AbstractModelFactory(ExecutableModelFactory parent) {
			_parent = parent;
		}

		@Override
		public Context<Class<?>, Member> newContext(String name, Composite<Class<?>, Member> parent, Class<?> clazz) {
			return _parent.newContext(name, parent, clazz);
		}

		@Override
		public Command<Class<?>, Member> newCommand(ConverterFactory converterFactory, Composite<Class<?>, Member> parent, String name, Method method) {
			return _parent.newCommand(converterFactory, parent, name, method);
		}

		@Override
		public Option<Class<?>, Member> newOption(ConverterFactory factory, Composite<Class<?>, Member> parent, String name, Member method) {
			return _parent.newOption(factory, parent, name, method);
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

	static abstract class Cache<E> {
		Map<Class<E>, E> _objects = new HashMap<Class<E>, E>();

		@SuppressWarnings("unchecked")
		public E newFactory(Class<? extends E> clazz) {
			if (_objects.containsKey(clazz))
				return _objects.get(clazz);
			try {
				E o = newItem((Class<E>) clazz);
				_objects.put((Class<E>) clazz, o);
				return o;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(String.format("Cannot instantiate factory: %s", clazz));
			}
		}

		abstract E newItem(Class<E> clazz) throws InstantiationException, IllegalAccessException; // clazz.newInstance()
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
			return this;
		}
	}

	public static Object[] convertArray(String cmd, Converter<?>[] converters, Executor executor) {
		Object[] res = new Object[converters.length];
		int i = 0;
		for (Converter<?> converter : converters)
			res[i++] = converter.convert(executor);
		return res;
	}

	public static ConverterFactory getComposite(ConverterFactory parent, Class<? extends ConverterFactory>[] newFactory) {
		ConverterFactory[] factories = new ConverterFactory[newFactory.length];
		for (int i = 0; i < newFactory.length; i++)
			factories[i] = cache.newFactory(newFactory[i]);
		return new ConverterFactory.CompositeFactory(factories, parent);
	}

	static Converter<?>[] getConverters(ConverterFactory factory, Class<?>[] params, Annotation[][] annotations) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		for (Class<?> a : params) {
			ConverterFactory f = factory;
			Class<? extends ConverterFactory> c;
			Param pa = AnnotationUtils.getAnnotation(annotations[i], Param.class);
			if (pa != null && !ConverterFactory.class.equals(c = pa.converter()))
				f = cache.newFactory(c);

			converters[i++] = ((f == null) ? factory : f).getConverter(a);
		}
		return converters;
	}

	static Converter<?>[] getConverters(ConverterFactory factory, Class<?>[] params) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			converters[i++] = factory.getConverter(a);
		return converters;
	}

	static boolean isEnclosed(Class<?> clazz) {
		return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
	}

	public final static ReflectValue<String> SUMMARY = new ReflectValue<String>("summary", fr.labri.shelly.annotations.Option.NO_NAME);
	public static class ReflectValue<V> extends AnnotationValue<AnnotatedElement, V> {
		public ReflectValue(String field, V value) {
			super(field, value);
		}

		@Override
		public Annotation getAnnotation(AnnotatedElement elt, Class<? extends Annotation> a) {
			return elt.getAnnotation(a);
		}
		
	}
	final public static AnnotationType<AnnotatedElement> REFLECT = new AnnotationType<AnnotatedElement>() {
		@Override
		public <A extends Annotation> A getValue(AnnotatedElement elt, Class<? extends A> a) {
			return elt.getAnnotation(a);
		}
	};
};
