package fr.labri.shelly.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Context;
import fr.labri.shelly.Description;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.impl.AbstractOption.OptionAdapter;

class OptionFactory {

	static AbstractOption build(ConverterFactory factory, Context parent, String name, final Field field) {
		return AbstractOption.getOption(name, parent, factory.getConverter(field.getType(), name), new OptionAdapter() {
			@Override
			public void apply(AbstractOption opt, Object receive, String next, PeekIterator<String> cmdline) {
				try {
					field.set(receive, opt._converter.convert(next, cmdline));
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
		return AbstractOption.getOption(name, parent, factory.getConverter(method.getParameterTypes()[0], name), new OptionAdapter() { // FIXME not robust
			@Override
			public void apply(AbstractOption opt, Object receive, String next, PeekIterator<String> cmdline) {
				try {
					method.invoke(receive, opt._converter.convert(next, cmdline));
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