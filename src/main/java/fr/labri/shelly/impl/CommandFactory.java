package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Context;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Default;

class CommandFactory {
	public interface CommandAdapter {
		public Object apply(AbstractCommand cmd, Object receive, String next, PeekIterator<String> cmdline);
		public Description getDescription();
		public boolean isDefault();
	}

	static AbstractCommand getCommand(String name, Context parent, Converter<?>[] converters, final CommandAdapter adapter) {
		return new AbstractCommand(name, parent, converters) {
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

	public static ShellyItem build(ConverterFactory loadFactory, Context parent, String name, final Method method) {
		return getCommand(name, parent, fr.labri.shelly.impl.ConverterFactory.getConverters(loadFactory, method.getParameterTypes()), new CommandAdapter() {
			
			@Override
			public boolean isDefault() {
				return method.isAnnotationPresent(Default.class);
			}
			
			@Override
			public Object apply(AbstractCommand cmd, Object grp, String text, PeekIterator<String> cmdLine) {
				try {
					return method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(cmd._converters, text, cmdLine));
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
}