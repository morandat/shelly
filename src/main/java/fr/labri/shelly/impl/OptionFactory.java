package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Description;
import fr.labri.shelly.annotations.AnnotationUtils;

class OptionFactory {

	public interface OptionAdapter {
		Object apply(AbstractOption opt, Object receive, String next, fr.labri.shelly.impl.PeekIterator<String> _cmdline);
		
		Description getDescription();
	}

	public static AbstractOption getOption(String name, Context parent, Converter<?> converter, final OptionAdapter adapter) {
		return new AbstractOption(converter, parent, name) {
			@Override
			public Object apply(Object receive, String next, PeekIterator<String> _cmdline) {
				return adapter.apply(this, receive, next, _cmdline);
			}

			@Override
			public Description getDescription() {
				return adapter.getDescription();
			}
		};
	}
	
	static AbstractOption build(ConverterFactory factory, Context parent, String name, final Field field) {
		return getOption(name, parent, factory.getConverter(field.getType(), name), new OptionAdapter() {
			@Override
			public Object apply(AbstractOption opt, Object receive, String next, PeekIterator<String> cmdline) {
				try {
					Object o = opt._converter.convert(next, cmdline);
					field.set(receive, o);
					return o;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Description getDescription() {
				return DescriptionFactory.getDescription(field, AnnotationUtils.getOptionSummary(field));
			}
		});
	}

	static AbstractOption build(ConverterFactory factory, Context parent, String name, final Method method) {
		return getOption(name, parent, factory.getConverter(method.getParameterTypes()[0], name), new OptionAdapter() { // FIXME not robust
			@Override
			public Object apply(AbstractOption opt, Object receive, String next, PeekIterator<String> cmdline) {
				try {
					Object o = opt._converter.convert(next, cmdline);
					method.invoke(receive, opt._converter.convert(next, cmdline));
					return o;
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			@Override
			public Description getDescription() {
				return DescriptionFactory.getDescription(method, AnnotationUtils.getOptionSummary(method));
			}
		});
	};
}