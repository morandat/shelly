package fr.labri.shelly.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.impl.AbstractContext.ContextAdapter;

public abstract class ModelFactory {

	public static final ExecutableModelFactory EXECUTABLE_MODEL = new ExecutableModelFactory();
	
	public abstract Option newOption(ConverterFactory factory, Context parent, String name, Field field);
	public abstract Option newOption(ConverterFactory factory, Context parent, String name, final Method method);
	public abstract Command newCommand(ConverterFactory loadFactory, Context parent, String name, final Method method);
	public abstract Command newCommand(String name, Context parent, Converter<?>[] converters, final CommandAdapter adapter);
	
	public abstract Group newGroup(String name, Context parent, Class<?> clazz, final GroupAdapter adapter); 
	public abstract Context newContext(String name, Context parent, Class<?> clazz, final ContextAdapter adapter);

	public interface CommandAdapter {
		public Object apply(AbstractCommand cmd, Object receive, String next, PeekIterator<String> cmdline);
		public Description getDescription();
		public boolean isDefault();
	}

	public interface OptionAdapter {
		Object apply(Option opt, Object receive, Object value);
		Description getDescription();
	}

	public interface GroupAdapter {
		Object apply(AbstractGroup abstractGroup, Object receive, String next, PeekIterator<String> cmdline);
		boolean isDefault();
	}

	static class ExecutableModelFactory extends ModelFactory {
		public Context newContext(String name, Context parent, Class<?> clazz, final ContextAdapter adapter) {
			return new AbstractContext(parent, name, clazz) {
			};
		}

		public Group newGroup(String name, Context parent, Class<?> clazz, final GroupAdapter adapter) {
			return new AbstractGroup(parent, name, clazz) {
				@Override
				public Object apply(Object receive, String next, PeekIterator<String> cmdline) {
					if (adapter != null)
						return adapter.apply(this, receive, next, cmdline);
					return null;
				}

				@Override
				public boolean isDefault() {
					if (adapter != null)
						return adapter.isDefault();
					return false; // FIXME
				}
			};
		}

		public Option newOption(ConverterFactory factory, Context parent, String name, Field field) {
			Class<?> type = field.getType();
			if (typeIsBool(type))
				return newBooleanOption(name, parent, getFieldAdapter(field));
			return newOption(name, parent, factory.getConverter(type, true, name), getFieldAdapter(field));
		}

		public Option newOption(ConverterFactory factory, Context parent, String name, final Method method) {
			Class<?> type = method.getParameterTypes()[0];
			if (typeIsBool(type))
				return newBooleanOption(name, parent, getAccessorAdapter(method));
			return newOption(name, parent, factory.getConverter(type, true, name), getAccessorAdapter(method));
		}
		
		boolean typeIsBool(Class<?> clazz) {
			return (Boolean.class.equals(clazz) || boolean.class.equals(clazz));
		}


		public Command newCommand(String name, Context parent, Converter<?>[] converters, final CommandAdapter adapter) {
			return new AbstractCommand(name, parent) {
				@Override
				public Object apply(Object receive, String next, PeekIterator<String> cmdline) {
					return adapter.apply(this, receive, next, cmdline);
				}

				@Override
				public boolean isDefault() {
					return adapter.isDefault();
				}

				@Override
				public Description getDescription() {
					return adapter.getDescription();
				}
			};
		}

		public Command newCommand(ConverterFactory loadFactory, Context parent, String name, final Method method) {
			final Converter<?>[] converters = fr.labri.shelly.impl.ConverterFactory.getConverters(loadFactory, method.getParameterTypes());
			return newCommand(name, parent, converters, new CommandAdapter() {

				@Override
				public boolean isDefault() {
					return method.isAnnotationPresent(Default.class);
				}

				@Override
				public Object apply(AbstractCommand cmd, Object grp, String text, PeekIterator<String> cmdLine) {
					try {
						return method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(converters, text, cmdLine));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public Description getDescription() {
					return DescriptionFactory.getDescription(method, AnnotationUtils.getCommandSummary(method));
				}
			});
		}
	};

	public static Option newOption(String name, Context parent, final Converter<?> converter, final OptionAdapter adapter) {
		return new AbstractOption(parent, name) {
			@Override
			public Object apply(Object receive, String next, PeekIterator<String> cmdline) {
				Object o = converter.convert(next, cmdline);
				return adapter.apply(this, receive, o);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	public static Option newBooleanOption(String name, Context parent, final OptionAdapter adapter) {
		return new AbstractOption(parent, name) {
			@Override
			public boolean isValid(String str) {
				return ("--" + _id).equals(str) || ("--no-" + _id).equals(str);
			}

			@Override
			public Object apply(Object receive, String opt, PeekIterator<String> _cmdline) {
				return adapter.apply(this, receive, !opt.startsWith("--no-"));
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}

	static OptionAdapter getFieldAdapter(final Field field) {
		return new OptionAdapter() {
			@Override
			public Object apply(Option opt, Object receive, Object value) {
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
			public Object apply(Option opt, Object receive, Object value) {
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

	ModelFactory instantiateFactory(Class<? extends ModelFactory> clazz) {
		try {
			Constructor<? extends ModelFactory> c = clazz.getConstructor(ModelFactory.class);
			if(c != null)
				return c.newInstance(this);
			else
				return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return EXECUTABLE_MODEL;
		}
	}
	
	static class AbstractModelFactory extends ModelFactory  {
		final ModelFactory _parent;
		
		public AbstractModelFactory(ModelFactory parent) {
			_parent = parent;
		}
		
		@Override
		public Option newOption(ConverterFactory factory, Context parent, String name, Field field) {
			return _parent.newOption(factory, parent, name, field);
		}

		@Override
		public Option newOption(ConverterFactory factory, Context parent, String name, Method method) {
			return _parent.newOption(factory, parent, name, method);
		}

		@Override
		public Command newCommand(ConverterFactory loadFactory, Context parent, String name, Method method) {
			return _parent.newCommand(loadFactory, parent, name, method);
		}

		@Override
		public Command newCommand(String name, Context parent, Converter<?>[] converters, CommandAdapter adapter) {
			return _parent.newCommand(name, parent, converters, adapter);
		}

		@Override
		public Group newGroup(String name, Context parent, Class<?> clazz, GroupAdapter adapter) {
			return _parent.newGroup(name, parent, clazz, adapter);
		}

		@Override
		public Context newContext(String name, Context parent, Class<?> clazz, ContextAdapter adapter) {
			return _parent.newContext(name, parent, clazz, adapter);
		}
		
	}
}
