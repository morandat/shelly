package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.Context;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.impl.AbstractCommand.CommandAdapter;

class CommandFactory {
	public static ShellyItem build(ConverterFactory loadFactory, Context parent, String name, final Method method) {
		return AbstractCommand.getCommand(name, parent, fr.labri.shelly.impl.ConverterFactory.getConverters(loadFactory, method.getParameterTypes()), new CommandAdapter() {
			
			@Override
			public boolean isDefault() {
				return method.isAnnotationPresent(Default.class);
			}
			
			@Override
			public void apply(AbstractCommand cmd, Object grp, String text, PeekIterator<String> cmdLine) {
				try {
					method.invoke(grp, fr.labri.shelly.impl.ConverterFactory.convertArray(cmd._converters, text, cmdLine));
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