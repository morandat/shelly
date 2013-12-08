package fr.labri.shelly.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.labri.shelly.ConverterFactory;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
	String name() default NO_NAME;
	String summary() default NO_NAME;
	Class<? extends ConverterFactory> factory() default fr.labri.shelly.impl.ConverterFactory.class;
	
	public static final String NO_NAME = "";
}
