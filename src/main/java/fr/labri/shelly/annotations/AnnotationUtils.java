package fr.labri.shelly.annotations;

import java.lang.reflect.AnnotatedElement;

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
	
	static public String getName(String name, String dflt) {
		return !name.equals(fr.labri.shelly.annotations.Option.NO_NAME) ? name : dflt.toLowerCase();
	}
}
