package fr.labri.shelly.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Visitor;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.FoundCommand;

public class ExecutableModelFactory extends ModelFactory<Class<?>, Member> {

	public static final ExecutableModelFactory EXECUTABLE_MODEL = new ExecutableModelFactory();

	abstract static class OptionAdapter implements fr.labri.shelly.impl.AbstractOption.OptionAdapter<Class<?>, Member> {
		public abstract Object apply(Option<Class<?>, Member> opt, Object receive, Object value);
		public abstract Description getDescription();
	}

	public interface CommandAdapter {
		public abstract boolean isDefault();
		public abstract Object apply(AbstractCommand<Class<?>, Member> cmd, Object grp, String text, PeekIterator<String> cmdLine);
		public abstract Description getDescription(Command<Class<?>, Member> cmd);
	}

	protected static ExecutableModelFactory loadModelFactory(ExecutableModelFactory _parentFactory, Class<? extends ExecutableModelFactory> newFactory) {
		ExecutableModelFactory factory = _parentFactory;
		if(newFactory != null)
			factory = factory.instantiateFactory(newFactory);
		return factory;
	}

	abstract static class ContextAdapter implements fr.labri.shelly.Context.ContextAdapter<Class<?>, Member> {
		final Field _superThis;
		final Constructor<?> _ctor;

		ContextAdapter(Class<?> clazz) {
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

		@Override
		public Object newGroup(Object parent) {
			return ExecutableModelFactory.newGroup(_superThis != null, _ctor, parent);
		}

		@Override
		public Object getEnclosing(Object obj) {
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

	abstract static class GroupAdapter extends ContextAdapter implements fr.labri.shelly.Group.GroupAdapter<Class<?>, Member>, CommandAdapter {
		GroupAdapter(Class<?> clazz) {
			super(clazz);
		}

		@Override
		public Object newGroup(Object parent) {
			return ExecutableModelFactory.newGroup(_superThis != null, _ctor, parent);
		}
	}
	

	public Context<Class<?>, Member> newContext(String name, Context<Class<?>, Member> parent, Class<?> clazz,	final fr.labri.shelly.Context.ContextAdapter<Class<?>, Member> adapter) {
		return new AbstractContext<Class<?>, Member>(parent, name, clazz) {

			@Override
			public Object newGroup(Object parent) {
				return adapter.newGroup(parent);
			}

			@Override
			public Object getEnclosing(Object obj) {
				return adapter.getEnclosing(obj);
			}
		};
	}

	@Override
	public Context<Class<?>, Member> newContext(String name, Context<Class<?>, Member> parent, Class<?> clazz) {
		return newContext(name, parent, clazz, new ContextAdapter(clazz){
		});
	}

	public Group<Class<?>, Member> newGroup(String name, Context<Class<?>, Member> parent, final Class<?> clazz) {
		final GroupAdapter adapter = new GroupAdapter(clazz) {

			@Override
			public Object apply(Group<Class<?>, Member> abstractGroup, Object receive, PeekIterator<String> cmdline) {
				return receive;
			}

			@Override
			public boolean isDefault() {
				return clazz.isAnnotationPresent(Default.class);
			}

			@Override
			public Object apply(AbstractCommand<Class<?>, Member> cmd, Object grp, String text, PeekIterator<String> cmdLine) {
				cmdLine.next();
				return grp;
			}

			@Override
			public Description getDescription(Group<Class<?>, Member> group) {
				return DescriptionFactory.getGroupDescription(group, AnnotationUtils.getGroupSummary(clazz));
			}

			@Override
			public Description getDescription(Command<Class<?>, Member> cmd) {
				return getDescription((Group<Class<?>, Member>)cmd);
			}
		};
		
		return new AbstractGroup<Class<?>, Member>(parent, name, clazz) {
			@Override
			public Command<Class<?>, Member> getDefault() {
				try {
					Visitor<Class<?>, Member> v = new CommandVisitor<Class<?>, Member>() {
						@Override
						public void visit(Command<Class<?>, Member> grp) {
							if (grp.isDefault()) {
								throw new FoundCommand(grp);
							}
						}
					};
					visit_commands(v);
				} catch (FoundCommand e) {
					return e.cmd;
				}
				return null;
			}

			public Object createContext(Object parent) {
				return new InstVisitor().instantiate(this, parent);
			}

			public Object newGroup(Object parent) {
				return adapter.newGroup(parent);
			}

			@Override
			public boolean isDefault() {
				return adapter.isDefault();
			}

			@Override
			public Object apply(Object receive, String next, PeekIterator<String> _cmdline) {
				return adapter.apply(this, receive, _cmdline);
			}

			@Override
			public Object getEnclosing(Object parent) {
				return adapter.getEnclosing(parent);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription(this);
			}
		};
	}

	public static Option<Class<?>, Member> newOption(String name, Context<Class<?>, Member> parent, final Converter<?> converter,
			final fr.labri.shelly.impl.AbstractOption.OptionAdapter<Class<?>, Member> adapter) {
		return new AbstractOption<Class<?>, Member>(parent, name) {
			@Override
			public Object apply(Object receive, String next, PeekIterator<String> cmdline) {
				Object o = converter.convert(cmdline.next(), cmdline);
				return adapter.apply(this, receive, o);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	static Object newGroup(boolean enclosed, Constructor<?> ctor, Object parent) {
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

	public static Option<Class<?>, Member> newBooleanOption(String name, Context<Class<?>, Member> parent, final OptionAdapter adapter) {
		return new AbstractOption<Class<?>, Member>(parent, name) {
			@Override
			public boolean isValid(String str) {
				return ("--" + _id).equals(str) || ("--no-" + _id).equals(str);
			}

			@Override
			public Object apply(Object receive, String next, PeekIterator<String> _cmdline) {
				return adapter.apply(this, receive, !_cmdline.peek().startsWith("--no-"));
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	public Option<Class<?>, Member> newOption(ConverterFactory factory, Context<Class<?>, Member> parent, String name, final Member member) {
		if(member instanceof Field)
			return newOption(factory, parent, name, (Field)member);
		if(member instanceof Method)
			return newOption(factory, parent, name, (Method)member);
		throw new RuntimeException("Option cannot be associated with constructor");
	}
	public Option<Class<?>, Member> newOption(ConverterFactory factory, Context<Class<?>, Member> parent, String name, Field field) {
		Class<?> type = field.getType();
		if (typeIsBool(type))
			return newBooleanOption(name, parent, getFieldAdapter(field));
		return newOption(name, parent, factory.getConverter(type, true, name), getFieldAdapter(field));
	}

	public Option<Class<?>, Member> newOption(ConverterFactory factory, Context<Class<?>, Member> parent, String name, final Method method) {
		Class<?> type = method.getParameterTypes()[0];
		if (typeIsBool(type))
			return newBooleanOption(name, parent, getAccessorAdapter(method));
		return newOption(name, parent, factory.getConverter(type, true, name), getAccessorAdapter(method));
	}

	boolean typeIsBool(Class<?> clazz) {
		return (Boolean.class.equals(clazz) || boolean.class.equals(clazz));
	}

	public Command<Class<?>, Member> newCommand(String name, Context<Class<?>, Member> parent, Converter<?>[] converters,
			final CommandAdapter adapter) {
		return new AbstractCommand<Class<?>, Member>(name, parent) {
			@Override
			public Object apply(Object receive,  String next, PeekIterator<String> cmdline) {
				return adapter.apply(this, receive, cmdline.peek(), cmdline);
			}

			@Override
			public boolean isDefault() {
				return adapter.isDefault();
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription(this);
			}

			@Override
			public Object createContext(Object parent) {
				return new InstVisitor().instantiate(this, parent);
			}
		};
	}
	
	public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Context<Class<?>, Member> parent, String name, Member member) {
		return newCommand(loadFactory, parent, name, (Method)member); // FIXME check
	}
	
	public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Context<Class<?>, Member> parent, String name, final Method method) {
		final Converter<?>[] converters = fr.labri.shelly.impl.ConverterFactory.getConverters(loadFactory, method.getParameterTypes(),
				method.getParameterAnnotations());
		return newCommand(name, parent, converters, new CommandAdapter() {
			@Override
			public boolean isDefault() {
				return method.isAnnotationPresent(Default.class);
			}

			@Override
			public Object apply(AbstractCommand<Class<?>, Member> cmd, Object grp, String text, PeekIterator<String> cmdLine) {
				try {
					return method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(converters, text, cmdLine));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription(Command<Class<?>, Member> cmd) {
				return DescriptionFactory.getCommandDescription(method, AnnotationUtils.getCommandSummary(method));
			}
		});
	}

	static class InstVisitor extends fr.labri.shelly.impl.Visitor<Class<?>, Member> {
		private Object group;

		@Override
		public void visit(Group<Class<?>, Member> cmdGroup) {
		}

		@Override
		public void visit(Context<Class<?>, Member> ctx) {
			visit_parent(ctx);
			group = ctx.newGroup(group);
		}

		@Override
		public void visit(Command<Class<?>, Member> cmdGroup) {
			visit_parent(cmdGroup);
		}

		public Object instantiate(Command<Class<?>, Member> cmd, Object lastValidParent) {
			group = lastValidParent;
			if (cmd instanceof Group)
				visit((Context<Class<?>, Member>) (Group<Class<?>, Member>) cmd);
			else
				cmd.accept(this);
			return group;
		}
	}

	static OptionAdapter getFieldAdapter(final Field field) {
		return new OptionAdapter() {
			@Override
			public Object apply(Option<Class<?>, Member> opt, Object receive, Object value) {
				try {
					field.set(receive, value);
					return value;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription() {
				return DescriptionFactory.getDescription(field, AnnotationUtils.getOptionSummary(field));
			}
		};
	}

	static OptionAdapter getAccessorAdapter(final Method method) {
		return new OptionAdapter() { // FIXME not robust
			@Override
			public Object apply(Option<Class<?>, Member> opt, Object receive, Object value) {
				try {
					method.invoke(receive, value);
					return value;
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription() {
				return DescriptionFactory.getDescription(method, AnnotationUtils.getOptionSummary(method));
			}
		};
	}

	static class AbstractModelFactory extends ExecutableModelFactory {
		final ExecutableModelFactory _parent;

		public AbstractModelFactory(ExecutableModelFactory parent) {
			_parent = parent;
		}

		@Override
		public Option<Class<?>, Member> newOption(ConverterFactory factory, Context<Class<?>, Member> parent, String name, Field field) {
			return _parent.newOption(factory, parent, name, field);
		}

		@Override
		public Option<Class<?>, Member> newOption(ConverterFactory factory, Context<Class<?>, Member> parent, String name, Method method) {
			return _parent.newOption(factory, parent, name, method);
		}

		@Override
		public Command<Class<?>, Member> newCommand(ConverterFactory loadFactory, Context<Class<?>, Member> parent, String name, Method method) {
			return _parent.newCommand(loadFactory, parent, name, method);
		}

		@Override
		public Command<Class<?>, Member> newCommand(String name, Context<Class<?>, Member> parent, Converter<?>[] converters,
				CommandAdapter adapter) {
			return _parent.newCommand(name, parent, converters, adapter);
		}

		@Override
		public Group<Class<?>, Member> newGroup(String name, Context<Class<?>, Member> parent, Class<?> clazz) {
			return _parent.newGroup(name, parent, clazz);
		}

		@Override
		public Context<Class<?>, Member> newContext(String name, Context<Class<?>, Member> parent, Class<?> clazz,
				fr.labri.shelly.Context.ContextAdapter<Class<?>, Member> adapter) {
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
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			return EXECUTABLE_MODEL;
		}
	}
};