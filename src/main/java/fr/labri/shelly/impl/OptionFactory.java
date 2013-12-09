package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Description;
import fr.labri.shelly.Option;
import fr.labri.shelly.annotations.AnnotationUtils;

class OptionFactory {

	public interface OptionAdapter {
		Object apply(Option opt, Object receive, Object value);

		Description getDescription();

	}

	public static Option getOption(String name, Context parent, final Converter<?> converter, final OptionAdapter adapter) {
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

	public static Option getBooleanOption(String name, Context parent, final OptionAdapter adapter) {
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
	static boolean typeIsBool(Class<?> clazz) {
		return (Boolean.class.equals(clazz) || boolean.class.equals(clazz));
	}
	static Option build(ConverterFactory factory, Context parent, String name, Field field) {
		Class<?> type = field.getType();
		if(typeIsBool(type))
			return getBooleanOption(name, parent, getFieldAdapter(field));
		return getOption(name, parent, factory.getConverter(type, true, name), getFieldAdapter(field));
	}

	static Option build(ConverterFactory factory, Context parent, String name, final Method method) {
		Class<?> type = method.getParameterTypes()[0];
		if(typeIsBool(type))
			return getBooleanOption(name, parent, getAccessorAdapter(method));
		return getOption(name, parent, factory.getConverter(type, true, name), getAccessorAdapter(method));
	};
}