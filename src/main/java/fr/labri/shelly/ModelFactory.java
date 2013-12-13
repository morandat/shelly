package fr.labri.shelly;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public interface ModelFactory<C, M> {
	
	public abstract Context<C, M> newContext(String name, Composite<C, M> parent, C clazz);
	public abstract Group<C, M> newGroup(String name, Composite<C, M> parent, C clazz); 
	public abstract Command<C, M> newCommand(ConverterFactory converterFactory, Composite<C, M> parent, String name, M member);
	public abstract Option<C, M> newOption(ConverterFactory converterFactory, Composite<C, M> parent, String name, M member);
	

	static public final Class<fr.labri.shelly.annotations.Group> GROUP_CLASS = fr.labri.shelly.annotations.Group.class;
	static public final Class<fr.labri.shelly.annotations.Option> OPT_CLASS = fr.labri.shelly.annotations.Option.class;
	static public final Class<fr.labri.shelly.annotations.Command> CMD_CLASS = fr.labri.shelly.annotations.Command.class;
	static public final Class<fr.labri.shelly.annotations.Context> CONTEXT_CLASS = fr.labri.shelly.annotations.Context.class;
	
	static public final Class<fr.labri.shelly.annotations.Error> ERROR_CLASS = fr.labri.shelly.annotations.Error.class;
	static public final Class<fr.labri.shelly.annotations.Param> PARAM_CLASS = fr.labri.shelly.annotations.Param.class;
	static public final Class<fr.labri.shelly.annotations.Default> DEFAULT_CLASS = fr.labri.shelly.annotations.Default.class;
	static public final Class<fr.labri.shelly.annotations.Description> DESCRIPTION_CLASS = fr.labri.shelly.annotations.Description.class;

	@SuppressWarnings("unchecked")
	final List<Class<? extends Annotation>> SHELLY_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList((Class<? extends Annotation>[]) new Class<?>[] {
			GROUP_CLASS, OPT_CLASS, CONTEXT_CLASS, CMD_CLASS, DESCRIPTION_CLASS, PARAM_CLASS, DEFAULT_CLASS, ERROR_CLASS }));
}
