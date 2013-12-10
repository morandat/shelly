package fr.labri.shelly.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import fr.labri.shelly.impl.ModelFactory;

public class AnnotationUtils {
	public static String getOptionName(AnnotatedElement elt) {
		Option o = elt.getAnnotation(Option.class);
		if(o == null)
			return Option.NO_NAME;
		return o.name();
	}
	public static String getCommandName(AnnotatedElement elt) {
		Command o = elt.getAnnotation(Command.class);
		if(o == null)
			return Option.NO_NAME;
		return o.name();
	}
	public static String getGroupName(AnnotatedElement elt) {
		Group o = elt.getAnnotation(Group.class);
		if(o == null)
			return Option.NO_NAME;
		return o.name();
	}
	
	public static String getOptionSummary(AnnotatedElement elt) {
		Option o = elt.getAnnotation(Option.class);
		if(o == null)
			return Option.NO_NAME;
		return o.summary();
	}
	public static String getCommandSummary(AnnotatedElement elt) {
		Command o = elt.getAnnotation(Command.class);
		if(o == null)
			return Option.NO_NAME;
		return o.summary();
	}
	public static String getGroupSummary(AnnotatedElement elt) {
		Group o = elt.getAnnotation(Group.class);
		if(o == null)
			return Option.NO_NAME;
		return o.summary();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> _dummy) {
		for(Annotation a: annotations)
			if(a.annotationType().equals(_dummy))
				return (T)a;
		return null;
	}
	
	static public String getName(String name, String dflt) {
		return !name.equals(fr.labri.shelly.annotations.Option.NO_NAME) ? name : dflt.toLowerCase();
	}

	final public static Class<? extends ModelFactory> getFactory(Class<? extends ModelFactory> factory) {
		if(!ModelFactory.class.equals(factory))
			return factory;
		return null;
	}
	public static Class<? extends ModelFactory> getFactory(Command annotation) {
		return getFactory(annotation.factory());
	}

	public static Class<? extends ModelFactory> getFactory(Option annotation) {
		return getFactory(annotation.factory());
	}

	public static Class<? extends ModelFactory> getFactory(Group annotation) {
		return getFactory(annotation.factory());
	}
	
	public static Class<? extends ModelFactory> getFactory(Context annotation) {
		return getFactory(annotation.factory());
	}
}
